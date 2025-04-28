package com.web.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.web.common.util.CommonUtil;
import com.web.common.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

;

@Slf4j
@Service
public class WeatherService {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private RedisUtil redisUtil;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private CommonUtil commonUtil;

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

}
