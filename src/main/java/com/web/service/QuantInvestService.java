package com.web.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.web.common.util.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
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
            return fetchFundamentals(ticker);
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

        double pe = extractRaw(sd,  "trailingPE");
        double pb = extractRaw(dks, "priceToBook");
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

        if (pb <= 0 && pe <= 0) return null;

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
