package com.web.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.web.common.util.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class QuantInvestService {

    private static final Random RNG = new Random();

    // Redis 키
    private static final String KEY_VALUE_LIST    = "quantInvest:value:list";
    private static final String KEY_QUALITY_LIST  = "quantInvest:quality:list";
    private static final String KEY_MOMENTUM_LIST = "quantInvest:momentum:list";
    private static final String KEY_SMALLCAP_LIST = "quantInvest:smallcap:list";
    private static final String KEY_META          = "quantInvest:meta";
    private static final int    CACHE_TTL         = 25 * 60 * 60; // 25시간 (매일 갱신 스케줄 커버)

    private static final String UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
            + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36";

    // crumb 메모리 캐시 (55분)
    private volatile String cachedCrumb    = null;
    private volatile String cachedCookie   = null;
    private volatile long   crumbFetchedAt = 0L;
    private static final long CRUMB_TTL_MS = 55 * 60 * 1000L;

    @Autowired private ObjectMapper objectMapper;
    @Autowired private CommonUtil   commonUtil;

    // ─────────────────────────────────────────────
    //  스케줄 (매일 오전 7시 KST = UTC 22:00)
    // ─────────────────────────────────────────────
    @Scheduled(cron = "0 0 22 * * *")
    public void scheduledRefresh() {
        log.info("[QuantInvest] 스케줄 갱신 시작");
        try { refreshData(); }
        catch (Exception e) { log.error("[QuantInvest] 스케줄 갱신 실패: {}", e.getMessage()); }
    }

    // ─────────────────────────────────────────────
    //  Wikipedia S&P 500 티커 수집
    // ─────────────────────────────────────────────
    private List<Map<String, String>> scrapeSpTickers() throws Exception {
        List<Map<String, String>> result = new ArrayList<>();
        Document doc = Jsoup.connect("https://en.wikipedia.org/wiki/List_of_S%26P_500_companies")
                .userAgent(UA).timeout(15000).get();

        Element table = doc.select("table#constituents").first();
        if (table == null) throw new Exception("Wikipedia S&P 500 테이블을 찾을 수 없습니다.");

        for (Element row : table.select("tbody tr")) {
            Elements cells = row.select("td");
            if (cells.size() < 3) continue;
            String symbol = cells.get(0).text().trim().replace(".", "-");
            String name   = cells.get(1).text().trim();
            String sector = cells.get(2).text().trim();
            if (!symbol.isEmpty()) {
                Map<String, String> e = new HashMap<>();
                e.put("symbol", symbol); e.put("name", name); e.put("sector", sector);
                result.add(e);
            }
        }
        log.info("[QuantInvest] Wikipedia {}개 티커 수집", result.size());
        return result;
    }

    // ─────────────────────────────────────────────
    //  전체 수집 · 네 전략 동시 저장
    // ─────────────────────────────────────────────
    public Map<String, Object> refreshData() throws Exception {
        List<Map<String, String>> tickerList = scrapeSpTickers();
        log.info("[QuantInvest] 수집 시작 - 총 {}개", tickerList.size());

        refreshCrumbIfNeeded();

        List<Map<String, Object>> raw = new ArrayList<>();

        for (int i = 0; i < tickerList.size(); i++) {
            String ticker     = tickerList.get(i).get("symbol");
            String wikiSector = tickerList.get(i).get("sector");

            try {
                Map<String, Object> stock = fetchFundamentals(ticker);
                if (stock != null) {
                    if ("-".equals(stock.get("sector")) || stock.get("sector") == null)
                        stock.put("sector", wikiSector);
                    raw.add(stock);
                    log.info("[QuantInvest] [{}/{}] {} PE={} PB={} ROE={} GPA={}",
                            i + 1, tickerList.size(), ticker,
                            stock.get("pe"), stock.get("pb"),
                            stock.get("roe"), stock.get("gpa"));
                }
            } catch (Exception e) {
                log.warn("[QuantInvest] [{}/{}] {} 실패: {}", i + 1, tickerList.size(), ticker, e.getMessage());
            }

            long delay = 700 + RNG.nextInt(600);
            if ((i + 1) % 20 == 0) {
                delay += 3000 + RNG.nextInt(3000);
                log.info("[QuantInvest] [{}/{}] 추가 대기 {}ms", i + 1, tickerList.size(), delay);
            }
            Thread.sleep(delay);
        }

        // ① 가치 전략: Low PER + Low PBR
        List<Map<String, Object>> valueList = scoreValue(
                raw.stream().filter(s -> pe(s) > 0 && pb(s) > 0)
                   .map(LinkedHashMap::new).collect(Collectors.toList())
        );

        // ② 퀄리티 전략: High ROE + High GP/A
        List<Map<String, Object>> qualityList = scoreQuality(
                raw.stream().filter(s -> roe(s) > 0 || gpa(s) > 0)
                   .map(LinkedHashMap::new).collect(Collectors.toList())
        );

        // ③ 모멘텀 전략: High 52-week Return + High Revenue Growth
        List<Map<String, Object>> momentumList = scoreMomentum(
                raw.stream().filter(s -> w52(s) != 0 && revGrowth(s) != 0)
                   .map(LinkedHashMap::new).collect(Collectors.toList())
        );

        // ④ 소형주 전략: Low Market Cap + High Revenue Growth
        List<Map<String, Object>> smallCapList = scoreSmallCap(
                raw.stream().filter(s -> mc(s) > 0 && revGrowth(s) != 0)
                   .map(LinkedHashMap::new).collect(Collectors.toList())
        );

        commonUtil.setCache(KEY_VALUE_LIST,    objectMapper.writeValueAsString(valueList),    CACHE_TTL);
        commonUtil.setCache(KEY_QUALITY_LIST,  objectMapper.writeValueAsString(qualityList),  CACHE_TTL);
        commonUtil.setCache(KEY_MOMENTUM_LIST, objectMapper.writeValueAsString(momentumList), CACHE_TTL);
        commonUtil.setCache(KEY_SMALLCAP_LIST, objectMapper.writeValueAsString(smallCapList), CACHE_TTL);

        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("updatedAt",      CommonUtil.formatNow("yyyy-MM-dd HH:mm:ss"));
        meta.put("valueCount",     valueList.size());
        meta.put("qualityCount",   qualityList.size());
        meta.put("momentumCount",  momentumList.size());
        meta.put("smallCapCount",  smallCapList.size());
        meta.put("status",         "OK");
        commonUtil.setCache(KEY_META, objectMapper.writeValueAsString(meta), CACHE_TTL);

        log.info("[QuantInvest] 완료 - 가치{}개 퀄리티{}개 모멘텀{}개 소형주{}개",
                valueList.size(), qualityList.size(), momentumList.size(), smallCapList.size());
        return meta;
    }

    // ─────────────────────────────────────────────
    //  개별 종목 펀더멘털
    // ─────────────────────────────────────────────
    private Map<String, Object> fetchFundamentals(String ticker) throws Exception {
        return fetchFundamentals(ticker, true);
    }

    private Map<String, Object> fetchFundamentals(String ticker, boolean strict) throws Exception {
        String encoded    = URLEncoder.encode(ticker, "UTF-8");
        String crumbParam = URLEncoder.encode(cachedCrumb, "UTF-8");
        String modules = "summaryDetail%2CdefaultKeyStatistics%2Cprice"
                       + "%2CfinancialData%2CbalanceSheetHistory";
        String urlStr = "https://query2.finance.yahoo.com/v10/finance/quoteSummary/"
                + encoded + "?modules=" + modules + "&crumb=" + crumbParam;

        HttpURLConnection conn = openConn(urlStr);
        conn.setRequestProperty("Cookie",  cachedCookie);
        conn.setRequestProperty("Accept",  "application/json");
        conn.setRequestProperty("Referer", "https://finance.yahoo.com/");

        int code = conn.getResponseCode();
        if (code == 401 || code == 403) {
            log.warn("[QuantInvest] {} {} → crumb 재갱신 재시도", ticker, code);
            cachedCrumb = null;
            refreshCrumbIfNeeded();
            conn.disconnect();
            return fetchFundamentals(ticker, strict);
        }
        if (code != 200) throw new Exception("HTTP " + code + " for " + ticker);

        String body = readBody(conn);
        conn.disconnect();

        JSONObject result = new JSONObject(body)
                .getJSONObject("quoteSummary")
                .getJSONArray("result")
                .getJSONObject(0);

        JSONObject sd    = result.optJSONObject("summaryDetail");
        JSONObject dks   = result.optJSONObject("defaultKeyStatistics");
        JSONObject price = result.optJSONObject("price");
        JSONObject fd    = result.optJSONObject("financialData");
        JSONObject bsh   = result.optJSONObject("balanceSheetHistory");

        if (sd == null || dks == null) return null;

        double pe = extractRaw(sd, "trailingPE");
        if (pe <= 0) pe = extractRaw(sd, "forwardPE");

        double pb = extractRaw(dks, "priceToBook");
        if (pb <= 0) {
            double bookValue = extractRaw(dks, "bookValue");
            double rawPrice  = price != null ? extractRaw(price, "regularMarketPrice") : 0;
            if (bookValue > 0 && rawPrice > 0) pb = round2(rawPrice / bookValue);
        }

        double mcap = extractRaw(sd, "marketCap");

        double currentPrice = price != null ? extractRaw(price, "regularMarketPrice") : 0;
        String name         = price != null ? price.optString("shortName", ticker)    : ticker;
        String sector       = price != null ? price.optString("sector", "-")          : "-";

        double roe = fd != null ? extractRaw(fd, "returnOnEquity") * 100.0 : 0;

        double gpa = 0;
        if (fd != null && bsh != null) {
            double grossProfits = extractRaw(fd, "grossProfits");
            JSONArray stmts = bsh.optJSONArray("balanceSheetStatements");
            double totalAssets = (stmts != null && stmts.length() > 0)
                    ? extractRaw(stmts.getJSONObject(0), "totalAssets") : 0;
            if (grossProfits > 0 && totalAssets > 0)
                gpa = grossProfits / totalAssets * 100.0;
        }

        double fiftyTwoWeekChange = extractRaw(dks, "52WeekChange") * 100.0;
        double revenueGrowth      = fd != null ? extractRaw(fd, "revenueGrowth") * 100.0 : 0;

        String currency     = price != null ? price.optString("currency", "USD") : "USD";
        String exchangeName = price != null ? price.optString("exchangeName", "") : "";

        if (currentPrice <= 0) return null;
        if (strict && pb <= 0 && pe <= 0) return null;

        Map<String, Object> stock = new LinkedHashMap<>();
        stock.put("symbol",             ticker);
        stock.put("name",               name);
        stock.put("sector",             sector);
        stock.put("price",              round2(currentPrice));
        stock.put("pe",                 round2(pe));
        stock.put("pb",                 round2(pb));
        stock.put("roe",                round2(roe));
        stock.put("gpa",                round2(gpa));
        stock.put("fiftyTwoWeekChange", round2(fiftyTwoWeekChange));
        stock.put("revenueGrowth",      round2(revenueGrowth));
        stock.put("marketCap",          (long) mcap);
        stock.put("currency",           currency);
        stock.put("exchange",           exchangeName);
        return stock;
    }

    // ─────────────────────────────────────────────
    //  ① 가치 전략: Low PER + Low PBR
    // ─────────────────────────────────────────────
    private List<Map<String, Object>> scoreValue(List<Map<String, Object>> stocks) {
        int n = stocks.size();
        if (n == 0) return stocks;

        List<Map<String, Object>> byPe = stocks.stream()
                .sorted(Comparator.comparingDouble(s -> pe(s))).collect(Collectors.toList());
        List<Map<String, Object>> byPb = stocks.stream()
                .sorted(Comparator.comparingDouble(s -> pb(s))).collect(Collectors.toList());

        Map<String, Integer> rankPe = new HashMap<>(), rankPb = new HashMap<>();
        for (int i = 0; i < n; i++) rankPe.put(sym(byPe.get(i)), i + 1);
        for (int i = 0; i < n; i++) rankPb.put(sym(byPb.get(i)), i + 1);

        for (Map<String, Object> s : stocks) {
            int rpe = rankPe.getOrDefault(sym(s), n);
            int rpb = rankPb.getOrDefault(sym(s), n);
            double score = ((n - rpe + 1) + (n - rpb + 1)) / (2.0 * n) * 100.0;
            s.put("valueScore", round2(score));
        }

        List<Map<String, Object>> sorted = stocks.stream()
                .sorted(Comparator.comparingDouble(s -> -dbl(s, "valueScore")))
                .collect(Collectors.toList());
        for (int i = 0; i < sorted.size(); i++) sorted.get(i).put("rank", i + 1);
        return sorted;
    }

    // ─────────────────────────────────────────────
    //  ② 퀄리티 전략: High ROE + High GP/A
    // ─────────────────────────────────────────────
    private List<Map<String, Object>> scoreQuality(List<Map<String, Object>> stocks) {
        int n = stocks.size();
        if (n == 0) return stocks;

        // ROE 내림차순
        List<Map<String, Object>> byRoe = stocks.stream()
                .sorted(Comparator.comparingDouble(s -> -roe(s))).collect(Collectors.toList());
        // GP/A 내림차순
        List<Map<String, Object>> byGpa = stocks.stream()
                .sorted(Comparator.comparingDouble(s -> -gpa(s))).collect(Collectors.toList());

        Map<String, Integer> rankRoe = new HashMap<>(), rankGpa = new HashMap<>();
        for (int i = 0; i < n; i++) rankRoe.put(sym(byRoe.get(i)), i + 1);
        for (int i = 0; i < n; i++) rankGpa.put(sym(byGpa.get(i)), i + 1);

        for (Map<String, Object> s : stocks) {
            int rroe = rankRoe.getOrDefault(sym(s), n);
            int rgpa = rankGpa.getOrDefault(sym(s), n);
            double roeScore = (double)(n - rroe + 1) / n * 100.0;
            double gpaScore = (double)(n - rgpa + 1) / n * 100.0;
            // GP/A 데이터가 없으면 ROE 점수만 사용
            double qualityScore = gpa(s) > 0 ? (roeScore + gpaScore) / 2.0 : roeScore;
            s.put("roeScore",     round2(roeScore));
            s.put("gpaScore",     round2(gpaScore));
            s.put("qualityScore", round2(qualityScore));
        }

        List<Map<String, Object>> sorted = stocks.stream()
                .sorted(Comparator.comparingDouble(s -> -dbl(s, "qualityScore")))
                .collect(Collectors.toList());
        for (int i = 0; i < sorted.size(); i++) sorted.get(i).put("rank", i + 1);
        return sorted;
    }

    // ─────────────────────────────────────────────
    //  ③ 모멘텀 전략: High 52-week Return + High Revenue Growth
    // ─────────────────────────────────────────────
    private List<Map<String, Object>> scoreMomentum(List<Map<String, Object>> stocks) {
        int n = stocks.size();
        if (n == 0) return stocks;

        List<Map<String, Object>> byW52 = stocks.stream()
                .sorted(Comparator.comparingDouble(s -> -w52(s))).collect(Collectors.toList());
        List<Map<String, Object>> byRev = stocks.stream()
                .sorted(Comparator.comparingDouble(s -> -revGrowth(s))).collect(Collectors.toList());

        Map<String, Integer> rankW52 = new HashMap<>(), rankRev = new HashMap<>();
        for (int i = 0; i < n; i++) rankW52.put(sym(byW52.get(i)), i + 1);
        for (int i = 0; i < n; i++) rankRev.put(sym(byRev.get(i)),  i + 1);

        for (Map<String, Object> s : stocks) {
            int rw52 = rankW52.getOrDefault(sym(s), n);
            int rrev = rankRev.getOrDefault(sym(s), n);
            double priceScore    = (double)(n - rw52 + 1) / n * 100.0;
            double growthScore   = (double)(n - rrev + 1) / n * 100.0;
            double momentumScore = (priceScore + growthScore) / 2.0;
            s.put("priceScore",    round2(priceScore));
            s.put("growthScore",   round2(growthScore));
            s.put("momentumScore", round2(momentumScore));
        }

        List<Map<String, Object>> sorted = stocks.stream()
                .sorted(Comparator.comparingDouble(s -> -dbl(s, "momentumScore")))
                .collect(Collectors.toList());
        for (int i = 0; i < sorted.size(); i++) sorted.get(i).put("rank", i + 1);
        return sorted;
    }

    // ─────────────────────────────────────────────
    //  ④ 소형주 전략: Low Market Cap + High Revenue Growth
    // ─────────────────────────────────────────────
    private List<Map<String, Object>> scoreSmallCap(List<Map<String, Object>> stocks) {
        int n = stocks.size();
        if (n == 0) return stocks;

        // 시가총액 오름차순 (낮을수록 소형주 = 좋음)
        List<Map<String, Object>> byMc = stocks.stream()
                .sorted(Comparator.comparingLong(s -> mc(s))).collect(Collectors.toList());
        // 매출 성장률 내림차순 (높을수록 좋음)
        List<Map<String, Object>> byRev = stocks.stream()
                .sorted(Comparator.comparingDouble(s -> -revGrowth(s))).collect(Collectors.toList());

        Map<String, Integer> rankMc = new HashMap<>(), rankRev = new HashMap<>();
        for (int i = 0; i < n; i++) rankMc.put(sym(byMc.get(i)),   i + 1);
        for (int i = 0; i < n; i++) rankRev.put(sym(byRev.get(i)), i + 1);

        for (Map<String, Object> s : stocks) {
            int rmc  = rankMc.getOrDefault(sym(s),  n);
            int rrev = rankRev.getOrDefault(sym(s), n);
            double smallScore  = (double)(n - rmc  + 1) / n * 100.0;
            double growthScore = (double)(n - rrev + 1) / n * 100.0;
            double smallCapScore = (smallScore + growthScore) / 2.0;
            s.put("smallScore",    round2(smallScore));
            s.put("growthScore",   round2(growthScore));
            s.put("smallCapScore", round2(smallCapScore));
        }

        List<Map<String, Object>> sorted = stocks.stream()
                .sorted(Comparator.comparingDouble(s -> -dbl(s, "smallCapScore")))
                .collect(Collectors.toList());
        for (int i = 0; i < sorted.size(); i++) sorted.get(i).put("rank", i + 1);
        return sorted;
    }

    // ─────────────────────────────────────────────
    //  Yahoo Finance crumb + cookie 취득
    // ─────────────────────────────────────────────
    private synchronized void refreshCrumbIfNeeded() throws Exception {
        if (cachedCrumb != null && (System.currentTimeMillis() - crumbFetchedAt) < CRUMB_TTL_MS) return;
        log.info("[QuantInvest] crumb 갱신 중...");

        HttpURLConnection c1 = openConn("https://fc.yahoo.com");
        c1.setInstanceFollowRedirects(true);
        c1.connect();
        String cookie = extractCookies(c1);
        c1.disconnect();

        HttpURLConnection c2 = openConn("https://query2.finance.yahoo.com/v1/test/getcrumb");
        c2.setRequestProperty("Cookie", cookie);
        if (c2.getResponseCode() != 200) throw new Exception("crumb HTTP " + c2.getResponseCode());
        String crumb = readBody(c2).trim();
        c2.disconnect();

        if (crumb.isEmpty()) throw new Exception("crumb 값이 비어 있음");
        cachedCookie = cookie; cachedCrumb = crumb; crumbFetchedAt = System.currentTimeMillis();
        log.info("[QuantInvest] crumb 갱신 완료");
    }

    private String extractCookies(HttpURLConnection conn) {
        List<String> set = conn.getHeaderFields().get("Set-Cookie");
        if (set == null) return "";
        StringBuilder sb = new StringBuilder();
        for (String c : set) sb.append(c.split(";")[0]).append("; ");
        return sb.toString().trim();
    }

    // ─────────────────────────────────────────────
    //  종목 검색 — 6자리 코드: Naver / 그 외: Yahoo Finance
    // ─────────────────────────────────────────────
    public List<Map<String, Object>> searchStock(String q) throws Exception {
        if (q.matches("\\d{6}")) {
            List<Map<String, Object>> r = searchNaverByCode(q);
            if (!r.isEmpty()) return r;
        }
        return searchStockYahoo(q);
    }

    /** 6자리 종목코드로 Naver 모바일 basic API에서 종목명·거래소 조회 */
    private List<Map<String, Object>> searchNaverByCode(String code) {
        try {
            Map<String, Object> basic = fetchNaverBasic(code);
            if (basic == null) return Collections.emptyList();
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("symbol",   basic.get("symbol"));
            r.put("name",     basic.get("name"));
            r.put("exchange", basic.get("exchange"));
            r.put("type",     "Equity");
            return Collections.singletonList(r);
        } catch (Exception e) {
            log.warn("[QuantInvest] Naver code lookup 실패 {}: {}", code, e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<Map<String, Object>> searchStockYahoo(String q) throws Exception {
        String encoded = URLEncoder.encode(q.trim(), "UTF-8");
        String urlStr  = "https://query1.finance.yahoo.com/v1/finance/search?q=" + encoded
                + "&quotesCount=10&newsCount=0&enableFuzzyQuery=false&quotesQueryId=tss_match_phrase_query";
        HttpURLConnection conn = openConn(urlStr);
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Referer", "https://finance.yahoo.com/");
        int httpCode = conn.getResponseCode();
        if (httpCode != 200) throw new Exception("Yahoo search HTTP " + httpCode);
        String body = readBody(conn);
        conn.disconnect();

        JSONArray quotes = new JSONObject(body).optJSONArray("quotes");
        List<Map<String, Object>> result = new ArrayList<>();
        if (quotes != null) {
            for (int i = 0; i < quotes.length(); i++) {
                JSONObject item    = quotes.getJSONObject(i);
                String typeDisp    = item.optString("typeDisp", "");
                String symbol      = item.optString("symbol", "");
                if (symbol.isEmpty()) continue;
                if (!"Equity".equals(typeDisp) && !"ETF".equals(typeDisp)) continue;
                String longname  = item.optString("longname", "");
                String shortname = item.optString("shortname", "");
                Map<String, Object> r = new LinkedHashMap<>();
                r.put("symbol",   symbol);
                r.put("name",     !longname.isEmpty() ? longname : shortname);
                r.put("exchange", item.optString("exchDisp", item.optString("exchange", "")));
                r.put("type",     typeDisp);
                result.add(r);
            }
        }
        return result;
    }

    // ─────────────────────────────────────────────
    //  개별 종목 조회 — 한국: Naver Finance / 해외: Yahoo Finance
    // ─────────────────────────────────────────────
    public List<Map<String, Object>> getStockDetails(List<String> symbols) throws Exception {
        refreshCrumbIfNeeded(); // 한국 주식도 Yahoo fallback을 위해 항상 준비

        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < symbols.size(); i++) {
            String symbol = symbols.get(i).trim().toUpperCase();
            if (symbol.isEmpty()) continue;
            try {
                Map<String, Object> stock;
                if (isKoreanSymbol(symbol)) {
                    // 1차: Naver Finance (price + PER/PBR/EPS/BPS/배당)
                    stock = fetchNaverFundamentals(toNaverCode(symbol));
                    // 2차: 핵심 지표(PER/PBR) 누락 시 Yahoo Finance로 보완
                    if (stock == null || (dbl(stock, "pe") <= 0 && dbl(stock, "pb") <= 0)) {
                        log.info("[QuantInvest] {} Naver 데이터 불충분 → Yahoo Finance fallback", symbol);
                        Map<String, Object> yahoo = null;
                        try { yahoo = fetchFundamentals(symbol, false); } catch (Exception ignored) {}
                        if (stock == null) {
                            stock = yahoo;
                        } else if (yahoo != null) {
                            if (dbl(stock, "pe")  <= 0 && dbl(yahoo, "pe")  > 0) stock.put("pe",  yahoo.get("pe"));
                            if (dbl(stock, "pb")  <= 0 && dbl(yahoo, "pb")  > 0) stock.put("pb",  yahoo.get("pb"));
                            if (dbl(stock, "roe") == 0 && dbl(yahoo, "roe") != 0) stock.put("roe", yahoo.get("roe"));
                            if (dbl(stock, "fiftyTwoWeekChange") == 0)            stock.put("fiftyTwoWeekChange", yahoo.get("fiftyTwoWeekChange"));
                            if (dbl(stock, "revenueGrowth")      == 0)            stock.put("revenueGrowth",      yahoo.get("revenueGrowth"));
                        }
                    }
                } else {
                    stock = fetchFundamentals(symbol, false);
                }

                if (stock != null) {
                    result.add(stock);
                } else {
                    Map<String, Object> err = new LinkedHashMap<>();
                    err.put("symbol", symbol); err.put("error", "데이터를 찾을 수 없습니다");
                    result.add(err);
                }
            } catch (Exception e) {
                log.warn("[QuantInvest] getStockDetails {} 실패: {}", symbol, e.getMessage());
                Map<String, Object> err = new LinkedHashMap<>();
                err.put("symbol", symbol); err.put("error", e.getMessage());
                result.add(err);
            }
            if (i < symbols.size() - 1) Thread.sleep(300);
        }
        return result;
    }

    // ─────────────────────────────────────────────
    //  Naver Finance — 한국주식 펀더멘털
    // ─────────────────────────────────────────────
    private static final String NAVER_API = "https://m.stock.naver.com/api/stock";

    private boolean isKoreanSymbol(String s) {
        return s.matches("\\d{6}(\\.K[SQ])?$");
    }

    private String toNaverCode(String s) {
        return s.replaceAll("\\.K[SQ]$", "");
    }

    private HttpURLConnection openNaverConn(String urlStr) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", UA);
        conn.setRequestProperty("Referer", "https://m.stock.naver.com/");
        conn.setRequestProperty("Accept", "application/json, text/plain, */*");
        conn.setConnectTimeout(8000);
        conn.setReadTimeout(8000);
        return conn;
    }

    private double parseDoubleStr(String s) {
        if (s == null || s.isEmpty() || "-".equals(s) || "N/A".equals(s)) return 0;
        try { return Double.parseDouble(s.replace(",", "").replace("%", "").trim()); }
        catch (NumberFormatException e) { return 0; }
    }

    private long parseLongStr(String s) {
        if (s == null || s.isEmpty() || "-".equals(s)) return 0L;
        try { return Long.parseLong(s.replace(",", "").trim()); }
        catch (NumberFormatException e) { return 0L; }
    }

    private Map<String, Object> fetchNaverFundamentals(String code) throws Exception {
        Map<String, Object> stock = fetchNaverBasic(code);
        if (stock == null) return null;
        enrichNaverInformation(code, stock);   // HTML 스크래핑: PER, PBR, EPS, BPS, 배당
        try { enrichNaverFinance(code, stock); }
        catch (Exception e) { log.debug("[QuantInvest] Naver finance 실패 {}: {}", code, e.getMessage()); }
        return stock;
    }

    private Map<String, Object> fetchNaverBasic(String code) throws Exception {
        HttpURLConnection conn = openNaverConn(NAVER_API + "/" + code + "/basic");
        int httpCode = conn.getResponseCode();
        if (httpCode != 200) { conn.disconnect(); throw new Exception("Naver basic HTTP " + httpCode + " for " + code); }
        String body = readBody(conn);
        conn.disconnect();

        JSONObject j = new JSONObject(body);

        double price = parseDoubleStr(j.optString("closePrice", "0"));
        if (price <= 0) return null;

        double pe  = parseDoubleStr(j.optString("per", "0"));
        double pb  = parseDoubleStr(j.optString("pbr", "0"));
        double bps = parseDoubleStr(j.optString("bps", "0"));
        if (pb <= 0 && bps > 0) pb = round2(price / bps);

        long   mcap = parseLongStr(j.optString("marketValue", "0"));
        String name = j.optString("stockName", code);

        JSONObject et   = j.optJSONObject("stockExchangeType");
        String exchCode = et != null ? et.optString("code", "") : "";
        String exchName = et != null ? et.optString("name", "") : "";
        String symbol   = code + ("KOE".equals(exchCode) ? ".KQ" : ".KS");

        Map<String, Object> stock = new LinkedHashMap<>();
        stock.put("symbol",             symbol);
        stock.put("name",               name);
        stock.put("sector",             "");
        stock.put("price",              round2(price));
        stock.put("pe",                 round2(pe));
        stock.put("pb",                 round2(pb));
        stock.put("roe",                0.0);
        stock.put("gpa",                0.0);
        stock.put("fiftyTwoWeekChange", 0.0);
        stock.put("revenueGrowth",      0.0);
        stock.put("eps",                0.0);
        stock.put("bps",                0.0);
        stock.put("divYield",           0.0);
        stock.put("divPerShare",        0.0);
        stock.put("marketCap",          mcap);
        stock.put("currency",           "KRW");
        stock.put("exchange",           exchName);
        return stock;
    }

    /** Naver Finance /finance 에서 ROE, 매출성장률 보완 */
    private void enrichNaverFinance(String code, Map<String, Object> stock) throws Exception {
        HttpURLConnection conn = openNaverConn(NAVER_API + "/" + code + "/finance");
        if (conn.getResponseCode() != 200) { conn.disconnect(); return; }
        String body = readBody(conn);
        conn.disconnect();

        JSONObject root = new JSONObject(body);
        double roe = 0, revGrowth = 0;

        // 구조 1: stockFinanceSummary
        JSONObject summary = root.optJSONObject("stockFinanceSummary");
        if (summary != null) {
            JSONArray prof = summary.optJSONArray("profitabilityList");
            if (prof != null && prof.length() > 0) {
                JSONObject latest = prof.getJSONObject(prof.length() - 1);
                roe = parseDoubleStr(latest.optString("roe", "0"));
            }
            JSONArray growth = summary.optJSONArray("growthList");
            if (growth != null && growth.length() > 0) {
                JSONObject latest = growth.getJSONObject(growth.length() - 1);
                revGrowth = parseDoubleStr(latest.optString("revenue", "0"));
            }
        }

        // 구조 2: 연간 테이블 형태 (annualFinancialRatioList)
        if (roe == 0) {
            JSONArray ratioList = root.optJSONArray("annualFinancialRatioList");
            if (ratioList != null) {
                for (int i = 0; i < ratioList.length(); i++) {
                    JSONObject section = ratioList.getJSONObject(i);
                    JSONArray items = section.optJSONArray("financeItemList");
                    if (items == null) continue;
                    for (int j = 0; j < items.length(); j++) {
                        JSONObject item = items.getJSONObject(j);
                        String itemName = item.optString("name", "");
                        if (itemName.contains("ROE")) {
                            JSONArray vals = item.optJSONArray("value");
                            if (vals != null && vals.length() > 0)
                                roe = parseDoubleStr(vals.getString(vals.length() - 1));
                        }
                        if (itemName.contains("매출액증가율") || itemName.contains("매출성장")) {
                            JSONArray vals = item.optJSONArray("value");
                            if (vals != null && vals.length() > 0)
                                revGrowth = parseDoubleStr(vals.getString(vals.length() - 1));
                        }
                    }
                }
            }
        }

        if (roe != 0)      stock.put("roe",            round2(roe));
        if (revGrowth != 0) stock.put("revenueGrowth", round2(revGrowth));
    }

    /**
     * finance.naver.com/item/information.naver 스크래핑 — PER, PBR, EPS, BPS, 배당수익률, 주당배당금
     * execute() 로 raw bytes를 받아 EUC-KR로 명시 파싱 → 한글 레이블 깨짐 방지
     */
    private void enrichNaverInformation(String code, Map<String, Object> stock) {
        try {
            String url = "https://finance.naver.com/item/main.naver?code=" + code;
            Connection.Response resp = Jsoup.connect(url)
                    .userAgent(UA)
                    .header("Referer", "https://finance.naver.com/")
                    .header("Accept-Language", "ko-KR,ko;q=0.9")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .timeout(10000)
                    .ignoreContentType(true)
                    .execute();

            // 네이버 증권 페이지는 EUC-KR → 명시적 파싱으로 한글 레이블 보장
            Document doc = Jsoup.parse(new String(resp.bodyAsBytes(), "EUC-KR"), url);
            Map<String, String> data = extractTableData(doc);

            applyDouble(stock, "pe",          data, "PER",       "배");
            applyDouble(stock, "pb",          data, "PBR",       "배");
            applyDouble(stock, "eps",         data, "EPS",       "원");
            applyDouble(stock, "bps",         data, "BPS",       "원");
            applyDouble(stock, "divYield",    data, "배당수익률", "%");
            applyDouble(stock, "divPerShare", data, "주당배당금", "원");

            // 시가총액 보완 (단위: 억원)
            String mcapRaw = data.getOrDefault("시가총액", "");
            if (!mcapRaw.isEmpty()) {
                long mc억 = parseLongStr(mcapRaw.replace("억", "").replace(",", "").trim());
                if (mc억 > 0 && ((Number) stock.getOrDefault("marketCap", 0L)).longValue() == 0L)
                    stock.put("marketCap", mc억 * 100_000_000L);
            }

            log.info("[QuantInvest] information.naver {} → PER={} PBR={} EPS={} BPS={} divYield={}",
                    code, stock.get("pe"), stock.get("pb"), stock.get("eps"),
                    stock.get("bps"), stock.get("divYield"));

        } catch (Exception e) {
            log.warn("[QuantInvest] information.naver 스크래핑 실패 {}: {}", code, e.getMessage());
        }
    }

    /** 모든 테이블의 th→td 쌍을 label→value 맵으로 수집 */
    private Map<String, String> extractTableData(Document doc) {
        Map<String, String> data = new LinkedHashMap<>();
        for (Element row : doc.select("table tr")) {
            Elements ths = row.select("th");
            Elements tds = row.select("td");
            int n = Math.min(ths.size(), tds.size());
            for (int i = 0; i < n; i++) {
                String k = ths.get(i).text().trim().replaceAll("[*\\s]+$", "").trim();
                String v = tds.get(i).text().trim();
                if (!k.isEmpty() && !v.isEmpty() && !"-".equals(v) && !"N/A".equals(v))
                    data.put(k, v);
            }
        }
        // dl/dt/dd 구조도 수집 (일부 섹션)
        for (Element dl : doc.select("dl")) {
            Elements dts = dl.select("dt");
            Elements dds = dl.select("dd");
            int n = Math.min(dts.size(), dds.size());
            for (int i = 0; i < n; i++) {
                String k = dts.get(i).text().trim();
                String v = dds.get(i).text().trim();
                if (!k.isEmpty() && !v.isEmpty() && !"-".equals(v))
                    data.putIfAbsent(k, v);
            }
        }
        return data;
    }

    /** 단위 문자열을 제거한 뒤 숫자로 변환해 stock 맵에 저장 (기존값이 0인 경우만) */
    private void applyDouble(Map<String, Object> stock, String field,
                             Map<String, String> data, String label, String unit) {
        String raw = data.getOrDefault(label, "");
        if (raw.isEmpty()) return;
        double v = parseDoubleStr(raw.replace(unit, ""));
        if (v != 0) stock.put(field, round2(v));
    }

    // ─────────────────────────────────────────────
    //  페이징 조회 (공통)
    // ─────────────────────────────────────────────

    public Map<String, Object> getScreeningList(int page, int size, String sector, String search) throws Exception {
        return pagedResult(KEY_VALUE_LIST, page, size, sector, search);
    }

    public Map<String, Object> getQualityList(int page, int size, String sector, String search) throws Exception {
        return pagedResult(KEY_QUALITY_LIST, page, size, sector, search);
    }

    public Map<String, Object> getMomentumList(int page, int size, String sector, String search) throws Exception {
        return pagedResult(KEY_MOMENTUM_LIST, page, size, sector, search);
    }

    public Map<String, Object> getSmallCapList(int page, int size, String sector, String search) throws Exception {
        return pagedResult(KEY_SMALLCAP_LIST, page, size, sector, search);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getCombinedRankingList(int page, int size, String sector, String search) throws Exception {
        String valueJson    = commonUtil.getCache(KEY_VALUE_LIST);
        String qualityJson  = commonUtil.getCache(KEY_QUALITY_LIST);
        String momentumJson = commonUtil.getCache(KEY_MOMENTUM_LIST);
        String smallCapJson = commonUtil.getCache(KEY_SMALLCAP_LIST);
        String metaJson     = commonUtil.getCache(KEY_META);

        if (valueJson == null || qualityJson == null || momentumJson == null || smallCapJson == null) {
            Map<String, Object> res = new HashMap<>();
            res.put("items", Collections.emptyList());
            res.put("totalCount", 0); res.put("page", page);
            res.put("size", size);   res.put("totalPages", 0);
            res.put("updatedAt", null); res.put("status", "NO_DATA");
            return res;
        }

        TypeReference<List<Map<String, Object>>> tr = new TypeReference<List<Map<String, Object>>>(){};
        List<Map<String, Object>> valueAll = objectMapper.readValue(valueJson, tr);
        Map<String, Map<String, Object>> valueMap    = toSymbolMap(valueAll);
        Map<String, Map<String, Object>> qualityMap  = toSymbolMap(objectMapper.readValue(qualityJson, tr));
        Map<String, Map<String, Object>> momentumMap = toSymbolMap(objectMapper.readValue(momentumJson, tr));
        Map<String, Map<String, Object>> smallCapMap = toSymbolMap(objectMapper.readValue(smallCapJson, tr));

        // S&P 500 전체 시가총액 순위
        Map<String, Integer> mcapRankMap = new HashMap<>();
        List<Map<String, Object>> byMcap = valueAll.stream()
                .sorted(Comparator.comparingLong(s -> {
                    Object v = s.get("marketCap");
                    return v instanceof Number ? -((Number) v).longValue() : 0L;
                }))
                .collect(Collectors.toList());
        for (int i = 0; i < byMcap.size(); i++) mcapRankMap.put(sym(byMcap.get(i)), i + 1);

        // 네 전략 모두 존재하는 종목만 (inner join)
        List<Map<String, Object>> combined = new ArrayList<>();
        for (String symbol : valueMap.keySet()) {
            if (!qualityMap.containsKey(symbol) || !momentumMap.containsKey(symbol)
                    || !smallCapMap.containsKey(symbol)) continue;

            Map<String, Object> v  = valueMap.get(symbol);
            Map<String, Object> q  = qualityMap.get(symbol);
            Map<String, Object> mo = momentumMap.get(symbol);
            Map<String, Object> sc = smallCapMap.get(symbol);

            double vs   = dbl(v,  "valueScore");
            double qs   = dbl(q,  "qualityScore");
            double mos  = dbl(mo, "momentumScore");
            double scs  = dbl(sc, "smallCapScore");
            double totalScore = (vs + qs + mos + scs) / 4.0;

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("symbol",        symbol);
            row.put("name",          v.get("name"));
            row.put("sector",        v.get("sector"));
            row.put("price",         v.get("price"));
            row.put("marketCap",     v.get("marketCap"));
            row.put("mcapRank",      mcapRankMap.getOrDefault(symbol, 0));
            row.put("valueScore",    round2(vs));
            row.put("qualityScore",  round2(qs));
            row.put("momentumScore", round2(mos));
            row.put("smallCapScore", round2(scs));
            row.put("totalScore",    round2(totalScore));
            combined.add(row);
        }

        combined.sort(Comparator.comparingDouble(r -> -dbl(r, "totalScore")));
        for (int i = 0; i < combined.size(); i++) combined.get(i).put("rank", i + 1);

        List<Map<String, Object>> filtered = combined;
        if (sector != null && !sector.isEmpty() && !"ALL".equals(sector)) {
            filtered = combined.stream()
                    .filter(r -> sector.equalsIgnoreCase((String) r.get("sector")))
                    .collect(Collectors.toList());
        }
        if (search != null && !search.trim().isEmpty()) {
            String q = search.trim().toLowerCase();
            filtered = filtered.stream()
                    .filter(r -> String.valueOf(r.get("symbol")).toLowerCase().contains(q)
                              || String.valueOf(r.get("name")).toLowerCase().contains(q))
                    .collect(Collectors.toList());
        }

        int total = filtered.size(), totalPages = (total + size - 1) / size;
        int from  = Math.min(page * size, total), to = Math.min(from + size, total);

        Map<String, Object> meta = metaJson != null
                ? objectMapper.readValue(metaJson, new TypeReference<Map<String, Object>>(){})
                : new HashMap<>();

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("items",      filtered.subList(from, to));
        res.put("totalCount", total); res.put("page", page);
        res.put("size",       size);  res.put("totalPages", totalPages);
        res.put("updatedAt",  meta.getOrDefault("updatedAt", null));
        res.put("status",     "OK");
        return res;
    }

    private Map<String, Map<String, Object>> toSymbolMap(List<Map<String, Object>> list) {
        Map<String, Map<String, Object>> map = new LinkedHashMap<>();
        for (Map<String, Object> s : list) map.put(sym(s), s);
        return map;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> pagedResult(String key, int page, int size, String sector, String search) throws Exception {
        String listJson = commonUtil.getCache(key);
        String metaJson = commonUtil.getCache(KEY_META);

        if (listJson == null) {
            Map<String, Object> res = new HashMap<>();
            res.put("items", Collections.emptyList());
            res.put("totalCount", 0); res.put("page", page);
            res.put("size", size);   res.put("totalPages", 0);
            res.put("updatedAt", null); res.put("status", "NO_DATA");
            return res;
        }

        List<Map<String, Object>> all = objectMapper.readValue(listJson,
                new TypeReference<List<Map<String, Object>>>(){});

        if (sector != null && !sector.isEmpty() && !"ALL".equals(sector)) {
            all = all.stream()
                    .filter(s -> sector.equalsIgnoreCase((String) s.get("sector")))
                    .collect(Collectors.toList());
        }
        if (search != null && !search.trim().isEmpty()) {
            String q = search.trim().toLowerCase();
            all = all.stream()
                    .filter(s -> String.valueOf(s.get("symbol")).toLowerCase().contains(q)
                              || String.valueOf(s.get("name")).toLowerCase().contains(q))
                    .collect(Collectors.toList());
        }

        int total = all.size(), totalPages = (total + size - 1) / size;
        int from  = Math.min(page * size, total), to = Math.min(from + size, total);

        Map<String, Object> meta = metaJson != null
                ? objectMapper.readValue(metaJson, new TypeReference<Map<String, Object>>(){})
                : new HashMap<>();

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("items",      all.subList(from, to));
        res.put("totalCount", total); res.put("page", page);
        res.put("size",       size);  res.put("totalPages", totalPages);
        res.put("updatedAt",  meta.getOrDefault("updatedAt", null));
        res.put("status",     "OK");
        return res;
    }

    @SuppressWarnings("unchecked")
    public List<String> getSectors(String strategy) throws Exception {
        String key = "quality".equals(strategy)   ? KEY_QUALITY_LIST
                   : "momentum".equals(strategy)  ? KEY_MOMENTUM_LIST
                   : "smallcap".equals(strategy)  ? KEY_SMALLCAP_LIST
                   : KEY_VALUE_LIST;
        String listJson = commonUtil.getCache(key);
        if (listJson == null) return Collections.emptyList();
        List<Map<String, Object>> all = objectMapper.readValue(listJson,
                new TypeReference<List<Map<String, Object>>>(){});
        return all.stream().map(s -> (String) s.get("sector"))
                .filter(s -> s != null && !s.isEmpty() && !"-".equals(s))
                .distinct().sorted().collect(Collectors.toList());
    }

    public Map<String, Object> getStatus() {
        String metaJson = commonUtil.getCache(KEY_META);
        if (metaJson == null) {
            Map<String, Object> res = new HashMap<>();
            res.put("status", "NO_DATA"); res.put("updatedAt", null); res.put("totalCount", 0);
            return res;
        }
        try { return objectMapper.readValue(metaJson, new TypeReference<Map<String, Object>>(){}); }
        catch (Exception e) { Map<String, Object> r = new HashMap<>(); r.put("status", "ERROR"); return r; }
    }

    // ─────────────────────────────────────────────
    //  공통 헬퍼
    // ─────────────────────────────────────────────
    private HttpURLConnection openConn(String urlStr) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", UA);
        conn.setRequestProperty("Accept-Language", "en-US,en;q=0.9");
        conn.setConnectTimeout(8000); conn.setReadTimeout(8000);
        return conn;
    }

    private String readBody(HttpURLConnection conn) throws Exception {
        int code = conn.getResponseCode();
        BufferedReader rd = new BufferedReader(new InputStreamReader(
                code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream(), "UTF-8"));
        StringBuilder sb = new StringBuilder(); String line;
        while ((line = rd.readLine()) != null) sb.append(line);
        rd.close(); return sb.toString();
    }

    private double extractRaw(JSONObject obj, String field) {
        if (!obj.has(field) || obj.isNull(field)) return 0;
        Object val = obj.get(field);
        if (val instanceof JSONObject) return ((JSONObject) val).optDouble("raw", 0);
        if (val instanceof Number)     return ((Number) val).doubleValue();
        return 0;
    }

    private double pe(Map<String, Object> s)        { return dbl(s, "pe"); }
    private double pb(Map<String, Object> s)        { return dbl(s, "pb"); }
    private double roe(Map<String, Object> s)       { return dbl(s, "roe"); }
    private double gpa(Map<String, Object> s)       { return dbl(s, "gpa"); }
    private double w52(Map<String, Object> s)       { return dbl(s, "fiftyTwoWeekChange"); }
    private double revGrowth(Map<String, Object> s) { return dbl(s, "revenueGrowth"); }
    private long   mc(Map<String, Object> s)        { Object v = s.get("marketCap"); return v instanceof Number ? ((Number)v).longValue() : 0L; }
    private String sym(Map<String, Object> s)       { return (String) s.get("symbol"); }
    private double dbl(Map<String, Object> s, String k) {
        Object v = s.get(k); return v instanceof Number ? ((Number) v).doubleValue() : 0;
    }
    private double round2(double v) { return Math.round(v * 100.0) / 100.0; }
}
