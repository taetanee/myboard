package com.web.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.web.common.util.CommonUtil;
import com.web.common.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;



@Slf4j
@Service
public class MyDashboardService {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private RedisUtil redisUtil;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private CommonUtil commonUtil;

	private final RestTemplate restTemplate = new RestTemplate();

	private static String serviceKey = "vvSbtDzTIbQ9rNkwq8WqL9SYwjihCcEujiNogCS9sgk37RU%2B3KJIRoQ6b%2FpY452SbKenj5A3RnPdgyup1jillw%3D%3D";

	public Map<String, Object> getCurrentSeoulWeather() throws Exception {

		String cacheKey = "cache:seoul_weather";
		ObjectMapper mapper = new ObjectMapper();

		//[시작] 캐시 확인
		String cachedData = commonUtil.getCache(cacheKey);
		if (cachedData != null) {
			return mapper.readValue(cachedData, Map.class);
		}
		//[종료] 캐시 확인

		//[시작] base_date, base_time 계산
		String[] baseTimes = { "0200", "0500", "0800", "1100", "1400", "1700", "2000", "2300" };
		LocalDateTime now = LocalDateTime.now();
		String baseDate = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		String baseTime = baseTimes[0];

		for (int i = baseTimes.length - 1; i >= 0; i--) {
			int hour = Integer.parseInt(baseTimes[i].substring(0, 2));
			if (now.getHour() >= hour) {
				baseTime = baseTimes[i];
				break;
			}
		}
		if (now.getHour() < 2) {
			baseDate = now.minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
			baseTime = "2300";
		}
		//[종료] base_date, base_time 계산

		//[시작] API 요청
		String nx = "60";
		String ny = "127";
		StringBuilder urlBuilder = new StringBuilder("http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtNcst");
		urlBuilder.append("?").append(URLEncoder.encode("serviceKey", "UTF-8")).append("=").append(serviceKey);
		urlBuilder.append("&").append(URLEncoder.encode("pageNo", "UTF-8")).append("=1");
		urlBuilder.append("&").append(URLEncoder.encode("numOfRows", "UTF-8")).append("=100");
		urlBuilder.append("&").append(URLEncoder.encode("dataType", "UTF-8")).append("=JSON");
		urlBuilder.append("&").append(URLEncoder.encode("base_date", "UTF-8")).append("=").append(baseDate);
		urlBuilder.append("&").append(URLEncoder.encode("base_time", "UTF-8")).append("=").append(baseTime);
		urlBuilder.append("&").append(URLEncoder.encode("nx", "UTF-8")).append("=").append(nx);
		urlBuilder.append("&").append(URLEncoder.encode("ny", "UTF-8")).append("=").append(ny);

		URL url = new URL(urlBuilder.toString());
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");

		BufferedReader rd = (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300)
				? new BufferedReader(new InputStreamReader(conn.getInputStream()))
				: new BufferedReader(new InputStreamReader(conn.getErrorStream()));

		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = rd.readLine()) != null) sb.append(line);
		rd.close();
		conn.disconnect();
		//[종료] API 요청

		//[시작] JSON 파싱
		JsonNode items = mapper.readTree(sb.toString()).path("response").path("body").path("items").path("item");

		double temperature = -999;
		int rainType = -1;

		for (JsonNode item : items) {
			String category = item.path("category").asText();
			String value = item.path("obsrValue").asText();

			if ("T1H".equals(category)) {
				temperature = Double.parseDouble(value);
			} else if ("PTY".equals(category)) {
				rainType = Integer.parseInt(value);
			}
		}
		//[종료] JSON 파싱

		//[시작] 강수 텍스트/설명 정의
		String rainText;
		String rainDesc;
		switch (rainType) {
			case 0: rainText = "없음"; rainDesc = "비가 오지 않음"; break;
			case 1: rainText = "비"; rainDesc = "비가 내림"; break;
			case 2: rainText = "비/눈"; rainDesc = "비와 눈이 섞여 내림"; break;
			case 3: rainText = "눈"; rainDesc = "눈이 내림"; break;
			case 4: rainText = "소나기"; rainDesc = "소나기가 내림"; break;
			default: rainText = "알 수 없음"; rainDesc = "날씨 상태를 알 수 없음";
		}
		//[종료] 강수 텍스트/설명 정의

		//[시작] 응답 구성
		Map<String, Object> response = new HashMap<>();
		response.put("location", "서울");

		Map<String, Object> tempMap = new HashMap<>();
		tempMap.put("value", temperature);
		tempMap.put("unit", "°C");

		Map<String, Object> rainMap = new HashMap<>();
		rainMap.put("type", rainText);
		rainMap.put("description", rainDesc);

		response.put("temperature", tempMap);
		response.put("precipitation", rainMap);
		//[종료] 응답 구성

		//[시작] 캐시 저장
		commonUtil.setCache(cacheKey, mapper.writeValueAsString(response), 600);
		//[종료] 캐시 저장

		return response;
	}

	public String getMinuDustFrcstDspth(HashMap<String, Object> _param) throws Exception {

		String cacheKey = "cache:minu_dust_seoul";

		//[시작] 캐시 확인
		String cachedData = commonUtil.getCache(cacheKey);
		if (cachedData != null) return cachedData;
		//[종료] 캐시 확인

		String result = "";

		//[시작] 하루 빼기
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1);
		String searchDate = sdf.format(cal.getTime());
		//[종료] 하루 빼기

		//[시작] API 호출
		StringBuilder urlBuilder = new StringBuilder("http://apis.data.go.kr/B552584/ArpltnInforInqireSvc/getMinuDustFrcstDspth");
		urlBuilder.append("?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + serviceKey);
		urlBuilder.append("&" + URLEncoder.encode("returnType", "UTF-8") + "=" + URLEncoder.encode("json", "UTF-8"));
		urlBuilder.append("&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode("100", "UTF-8"));
		urlBuilder.append("&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + URLEncoder.encode("1", "UTF-8"));
		urlBuilder.append("&" + URLEncoder.encode("searchDate", "UTF-8") + "=" + URLEncoder.encode(searchDate, "UTF-8"));
		urlBuilder.append("&" + URLEncoder.encode("InformCode", "UTF-8") + "=" + URLEncoder.encode("PM10", "UTF-8"));

		URL url = new URL(urlBuilder.toString());
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Content-type", "application/json");

		BufferedReader rd;
		if(conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
			rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		} else {
			rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
		}
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = rd.readLine()) != null) {
			sb.append(line);
		}
		rd.close();
		conn.disconnect();
		//[종료] API 호출

		//[시작] 값 가져오기
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode rootNode = objectMapper.readTree(sb.toString());
		JsonNode items = rootNode.path("response").path("body").path("items");

		String latestDate = "";
		for (JsonNode item : items) {
			String informData = item.path("informData").asText();
			if (informData.compareTo(latestDate) > 0) {
				latestDate = informData;
			}
		}

		for (JsonNode item : items) {
			if (latestDate.equals(item.path("informData").asText())) {
				String informGrade = item.path("informGrade").asText();
				for (String regionData : informGrade.split(",")) {
					if (regionData.startsWith("서울")) {
						result = regionData.split(":")[1].trim();
						break;
					}
				}
				break;
			}
		}
		//[종료] 값 가져오기

		//[시작] 캐시 저장
		if (result != null && !result.isEmpty()) {
			commonUtil.setCache(cacheKey, result, 600);
		}
		//[종료] 캐시 저장

		return result;
	}

	public Map<String, Object> getSnp500CurrentPrice() {
		String cacheKey = "cache:snp500_index";
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> result = new HashMap<>();

		try {
			// [시작] 캐시 확인
			String cachedData = commonUtil.getCache(cacheKey);
			if (cachedData != null) {
				return mapper.readValue(cachedData, Map.class);
			}
			// [종료] 캐시 확인

			// [시작] 크롤링 수행
			Document doc = Jsoup.connect("https://finance.yahoo.com/quote/%5EGSPC")
					.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
					.timeout(5000)
					.get();

			// 1. 현재 지수 (Price)
			Element priceEl = doc.selectFirst("fin-streamer[data-field=regularMarketPrice]");
			// 2. 등락값 (Change)
			Element changeEl = doc.selectFirst("fin-streamer[data-field=regularMarketChange]");
			// 3. 등락율 (Percent Change)
			Element percentEl = doc.selectFirst("fin-streamer[data-field=regularMarketChangePercent]");

			if (priceEl != null && changeEl != null && percentEl != null) {
				String price = priceEl.text();
				String change = changeEl.text();
				String percent = percentEl.text().replace("(", "").replace(")", ""); // 괄호 제거

				result.put("price", price);
				result.put("change", change);
				result.put("percent", percent);
				result.put("isUp", !change.startsWith("-"));

				// [시작] 캐시 저장
				// 야후 파이낸스 지수는 자주 바뀌므로 날씨보다는 짧은 캐시 시간을 권장합니다 (예: 1분~5분)
				commonUtil.setCache(cacheKey, mapper.writeValueAsString(result), 60);
				// [종료] 캐시 저장
			}
			// [종료] 크롤링 수행

		} catch (Exception e) {
			System.err.println("S&P 500 크롤링 실패: " + e.getMessage());
			result.put("price", "0.00");
			result.put("change", "0.00");
			result.put("percent", "0.00%");
			result.put("isUp", true);
			result.put("error", e.getMessage());
		}
		return result;
	}


	public Map<String, Object> getExchangeRateUSDToKRW() {
		String cacheKey = "cache:exchange_rate_usd_krw";
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> result = new HashMap<>();

		try {
			// [시작] 캐시 확인
			String cachedData = commonUtil.getCache(cacheKey);
			if (cachedData != null) {
				return mapper.readValue(cachedData, Map.class);
			}
			// [종료] 캐시 확인

			// [시작] API 요청
			String urlStr = "https://open.er-api.com/v6/latest/USD";
			URL url = new URL(urlStr);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(3000);

			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuilder response = new StringBuilder();
			String line;
			while ((line = rd.readLine()) != null) {
				response.append(line);
			}
			rd.close();

			JSONObject json = new JSONObject(response.toString());
			double currentRate = json.getJSONObject("rates").getDouble("KRW");

			// 전일 대비 계산 (임시 로직 유지)
			double dummyPrevClose = currentRate - 2.5;
			double change = currentRate - dummyPrevClose;
			double percent = (change / dummyPrevClose) * 100;

			result.put("rate", String.format("%.2f", currentRate));
			result.put("change", String.format("%.2f", change));
			result.put("percent", String.format("%.2f", percent) + "%");
			result.put("isUp", change >= 0);

			// [시작] 캐시 저장
			// 환율은 변동성이 있으므로 5분~10분 정도의 캐시를 추천합니다.
			commonUtil.setCache(cacheKey, mapper.writeValueAsString(result), 60);
			// [종료] 캐시 저장

		} catch (Exception e) {
			result.put("rate", "0.00");
			result.put("change", "0.00");
			result.put("percent", "0.00%");
			result.put("isUp", true);
			result.put("error", e.getMessage());
		}
		return result;
	}


	public Map<String, Object> getFearAndGreedIndex() {
		String cacheKey = "cache:fear_greed_index";
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> result = new HashMap<>();

		try {
			// [시작] 캐시 확인
			String cachedData = commonUtil.getCache(cacheKey);
			if (cachedData != null) {
				return mapper.readValue(cachedData, Map.class);
			}
			// [종료] 캐시 확인

			// [시작] API 요청
			URL url = new URL("https://production.dataviz.cnn.io/index/fearandgreed/graphdata");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");

			// 브라우저 환경 헤더 세팅
			conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
			conn.setRequestProperty("Accept", "application/json");
			conn.setRequestProperty("Referer", "https://www.cnn.com/markets/fear-and-greed");
			conn.setRequestProperty("Origin", "https://www.cnn.com");

			conn.setConnectTimeout(5000);
			conn.setReadTimeout(5000);

			int responseCode = conn.getResponseCode();
			if (responseCode != 200) {
				throw new Exception("CNN 차단됨 (HTTP " + responseCode + ")");
			}

			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = rd.readLine()) != null) sb.append(line);
			rd.close();

			// [시작] JSON 파싱 및 데이터 구성
			JSONObject json = new JSONObject(sb.toString());
			JSONObject fng = json.getJSONObject("fear_and_greed");

			double scoreDouble = fng.getDouble("score");
			int score = (int) Math.round(scoreDouble);
			String rating = fng.getString("rating");

			double previousClose = fng.getDouble("previous_close");
			int prevScore = (int) Math.round(previousClose);
			int diff = score - prevScore;

			result.put("value", score);
			result.put("rating", rating);
			result.put("prevValue", prevScore);
			result.put("diff", diff);
			result.put("status", diff >= 0 ? "UP" : "DOWN");
			// [종료] JSON 파싱 및 데이터 구성

			// [시작] 캐시 저장
			// 심리 지수는 변동폭이 크지 않으므로 10분~30분 정도의 캐시를 추천합니다.
			commonUtil.setCache(cacheKey, mapper.writeValueAsString(result), 10 * 60);
			// [종료] 캐시 저장

		} catch (Exception e) {
			result.put("value", 0);
			result.put("rating", "데이터 오류");
			result.put("diff", 0);
			result.put("status", "NONE");
			result.put("error", e.getMessage());
		}
		return result;
	}

	public Map<String, Object> getVixIndex() {
		String cacheKey = "cache:vix_index";
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> result = new HashMap<>();

		try {
			// [시작] 캐시 확인
			String cachedData = commonUtil.getCache(cacheKey);
			if (cachedData != null) {
				return mapper.readValue(cachedData, Map.class);
			}
			// [종료] 캐시 확인

			// [시작] API 요청
			URL url = new URL("https://query1.finance.yahoo.com/v8/finance/chart/^VIX?interval=1d");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
			conn.setConnectTimeout(5000);

			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = rd.readLine()) != null) sb.append(line);
			rd.close();

			// [시작] JSON 파싱
			JSONObject responseJson = new JSONObject(sb.toString());
			JSONObject meta = responseJson.getJSONObject("chart")
					.getJSONArray("result")
					.getJSONObject(0)
					.getJSONObject("meta");

			double cur = meta.getDouble("regularMarketPrice");
			// 전일 종가가 없을 경우 현재가나 기본값으로 대체하는 안전장치
			double pre = meta.optDouble("previousClose", meta.optDouble("chartPreviousClose", cur));
			double diff = cur - pre;

			result.put("price", String.format("%.2f", cur));
			result.put("change", String.format("%.2f", Math.abs(diff)));
			result.put("percent", String.format("%.2f%%", Math.abs((diff / pre) * 100)));
			result.put("isUp", diff >= 0);
			// [종료] JSON 파싱

			// [시작] 캐시 저장
			// VIX 지수는 시장 변동성을 나타내므로 5분(300초) 정도의 캐시가 적당합니다.
			commonUtil.setCache(cacheKey, mapper.writeValueAsString(result), 60 * 5);
			// [종료] 캐시 저장

		} catch (Exception e) {
			System.err.println("VIX 지수 로드 실패: " + e.getMessage());
			result.put("price", "0.00");
			result.put("change", "0.00");
			result.put("percent", "0.00%");
			result.put("isUp", false);
			result.put("error", e.getMessage());
		}
		return result;
	}
}
