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

;

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

	public String getMinuDustFrcstDspth(HashMap<String, Object> _param) throws Exception {

		String result = "";

		//[시작] 하루 빼기
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1); // 하루 빼기
		String searchDate = sdf.format(cal.getTime());
		//[종료] 하루 빼기

		//[시작] API 호출
		StringBuilder urlBuilder = new StringBuilder("http://apis.data.go.kr/B552584/ArpltnInforInqireSvc/getMinuDustFrcstDspth"); /*URL*/
		urlBuilder.append("?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + serviceKey); /*Service Key*/
		urlBuilder.append("&" + URLEncoder.encode("returnType", "UTF-8") + "=" + URLEncoder.encode("json", "UTF-8")); /*xml 또는 json*/
		urlBuilder.append("&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode("100", "UTF-8")); /*한 페이지 결과 수(조회 날짜로 검색 시 사용 안함)*/
		urlBuilder.append("&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + URLEncoder.encode("1", "UTF-8")); /*페이지번호(조회 날짜로 검색 시 사용 안함)*/
		urlBuilder.append("&" + URLEncoder.encode("searchDate", "UTF-8") + "=" + URLEncoder.encode(searchDate, "UTF-8"));
		urlBuilder.append("&" + URLEncoder.encode("InformCode", "UTF-8") + "=" + URLEncoder.encode("PM10", "UTF-8")); /*통보코드검색(PM10, PM25, O3)*/
		URL url = new URL(urlBuilder.toString());
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Content-type", "application/json");
		System.out.println("Response code: " + conn.getResponseCode());
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
		System.out.println(sb.toString());
		//[종료] API 호출

		//[시작] 값 가져오기
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode rootNode = objectMapper.readTree(sb.toString());
		JsonNode items = rootNode.path("response").path("body").path("items");

		// 가장 최신 날짜 구하기
		String latestDate = "";
		for (JsonNode item : items) {
			String informData = item.path("informData").asText();
			if (informData.compareTo(latestDate) > 0) {
				latestDate = informData;
			}
		}

		// 최신 날짜에 해당하는 서울 값 가져오기
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

		return result;
	}

	public double getSnp500CurrentPrice() {
		try {
			Document doc = Jsoup.connect("https://finance.yahoo.com/quote/%5EGSPC")
					.userAgent("Mozilla/5.0")
					.get();

			Element priceEl = doc.selectFirst("fin-streamer[data-field=regularMarketPrice]");
			if (priceEl != null) {
				String priceText = priceEl.text().replace(",", "").trim();
				if (!priceText.isEmpty()) {
					return Double.parseDouble(priceText);
				}
			}

			throw new RuntimeException("현재 가격을 찾을 수 없습니다.");
		} catch (Exception e) {
			throw new RuntimeException("현재 가격 추출 실패", e);
		}
	}


	public Map<String, Object> getCurrentSeoulWeather() throws Exception {
		// [1] base_date, base_time 계산
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

		// [2] API 요청
		String nx = "60"; // 서울 X
		String ny = "127"; // 서울 Y
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

		// [3] JSON 파싱
		ObjectMapper mapper = new ObjectMapper();
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

		// [4] 강수 텍스트/설명 정의
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

		// [5] 응답 구성
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

		return response;
	}


	public Map<String, Object> getExchangeRateUSDToKRW() {
		Map<String, Object> result = new HashMap<>();
		try {
			// 인베스팅닷컴 실시간 지수를 반영하는 공개 API 경로 (예시)
			// 실제 운영 시에는 인증된 환율 API 사용을 권장하지만, 테스트용으로 아래 구조를 사용합니다.
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

			// 전일 대비 계산 (API에서 제공하는 경우 사용, 없을 경우 임의 계산 예시)
			// 실제 인베스팅닷컴처럼 정밀하게 하려면 서버에서 어제 종가를 DB에 저장해둬야 합니다.
			// 여기서는 화면 구성을 위해 계산 로직만 넣어둡니다.
			double dummyPrevClose = currentRate - 2.5; // 테스트용 전일 종가 가정
			double change = currentRate - dummyPrevClose;
			double percent = (change / dummyPrevClose) * 100;

			result.put("rate", String.format("%.2f", currentRate));
			result.put("change", String.format("%.2f", change));
			result.put("percent", String.format("%.2f", percent) + "%");
			result.put("isUp", change >= 0);

		} catch (Exception e) {
			result.put("rate", "0.00");
			result.put("change", "0.00");
			result.put("percent", "0%");
			result.put("isUp", true);
		}
		return result;
	}
}
