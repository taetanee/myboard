package com.web.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.web.common.Const;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

@Component
@Slf4j
public class APIUtil {

    private static final String serviceKey = "vvSbtDzTIbQ9rNkwq8WqL9SYwjihCcEujiNogCS9sgk37RU%2B3KJIRoQ6b%2FpY452SbKenj5A3RnPdgyup1jillw%3D%3D";
    private static final String ShortWeatherURL = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtNcst";
    private static final String mediumTermWeatherURL = "http://apis.data.go.kr/1360000/MidFcstInfoService/getMidFcst";
    private static final String covidURL = "http://apis.data.go.kr/1352000/ODMS_COVID_04/callCovid04Api";
    private static final String stockURL = "https://apis.data.go.kr/1160100/service/GetStockSecuritiesInfoService";


    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private ObjectMapper objectMapper;

    public HashMap<String,Object> callAPIShortWeather(HashMap<String, Object> _param) throws Exception {

        HashMap<String, String> param = (HashMap<String, String>) _param.clone();
        if (commonUtil.isEmptyOrNull(param.get("base_date"))) {
            param.put("base_date", commonUtil.getMinusOneHour(commonUtil.getNow()).substring(0,8));
        }

        if (commonUtil.isEmptyOrNull(param.get("base_time"))) {
            param.put("base_time", commonUtil.getMinusOneHour(commonUtil.getNow()).substring(9,11) + "00");
        }

        param.put("pageNo",URLEncoder.encode("1", "UTF-8"));  /*페이지번호*/
        param.put("numOfRows",URLEncoder.encode("1000", "UTF-8"));  /*한 페이지 결과 수*/
        param.put("dataType",URLEncoder.encode("JSON", "UTF-8")); /*요청자료형식(XML/JSON) Default: XML*/
        param.put("base_date",URLEncoder.encode(param.get("base_date"), "UTF-8")); /*‘21년 6월 28일 발표*/
        param.put("base_time",URLEncoder.encode(param.get("base_time"), "UTF-8")); /*06시 발표(정시단위) */
        param.put("nx",URLEncoder.encode(Const.NX, "UTF-8")); /*예보지점의 X 좌표값*/
        param.put("ny",URLEncoder.encode(Const.NY, "UTF-8")); /*예보지점의 Y 좌표값*/

        StringBuilder urlBuilder = new StringBuilder(ShortWeatherURL);
        urlBuilder.append("?" + URLEncoder.encode("serviceKey","UTF-8") + "=" + serviceKey);
        urlBuilder.append("&" + URLEncoder.encode("pageNo","UTF-8") + "=" + param.get("pageNo"));
        urlBuilder.append("&" + URLEncoder.encode("numOfRows","UTF-8") + "=" + param.get("numOfRows"));
        urlBuilder.append("&" + URLEncoder.encode("dataType","UTF-8") + "=" + param.get("dataType"));
        urlBuilder.append("&" + URLEncoder.encode("base_date","UTF-8") + "=" + param.get("base_date"));
        urlBuilder.append("&" + URLEncoder.encode("base_time","UTF-8") + "=" + param.get("base_time"));
        urlBuilder.append("&" + URLEncoder.encode("nx","UTF-8") + "=" + param.get("nx"));
        urlBuilder.append("&" + URLEncoder.encode("ny","UTF-8") + "=" + param.get("ny"));
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
        log.info(sb.toString());
        return objectMapper.readValue(sb.toString(), HashMap.class);
    }

    private void callAPIMediumWeather(HashMap<String,String> _param) throws IOException {

        HashMap<String,String> param = (HashMap<String, String>) _param.clone();
        param.put("pageNo",URLEncoder.encode("1", "UTF-8"));  /*페이지번호*/
        param.put("numOfRows",URLEncoder.encode("10", "UTF-8"));  /*한 페이지 결과 수*/
        param.put("dataType",URLEncoder.encode("XML", "UTF-8")); /*요청자료형식(XML/JSON) Default: XML*/
        param.put("stnId",URLEncoder.encode("108", "UTF-8")); /*108 전국, 109 서울, 인천, 경기도 등 (활용가이드 하단 참고자료 참조)*/
        param.put("tmFc",URLEncoder.encode("202202060600", "UTF-8")); /*-일 2회(06:00,18:00)회 생성 되며 발표시각을 입력 YYYYMMDD0600 (1800)-최근 24시간 자료만 제공*/

        StringBuilder urlBuilder = new StringBuilder(mediumTermWeatherURL); /*URL*/
        urlBuilder.append("?" + URLEncoder.encode("serviceKey","UTF-8") + "="+serviceKey); /*Service Key*/
        urlBuilder.append("&" + URLEncoder.encode("pageNo","UTF-8") + "=" + param.get("pageNo"));
        urlBuilder.append("&" + URLEncoder.encode("numOfRows","UTF-8") + "=" + param.get("numOfRows"));
        urlBuilder.append("&" + URLEncoder.encode("dataType","UTF-8") + "=" + param.get("dataType"));
        urlBuilder.append("&" + URLEncoder.encode("stnId","UTF-8") + "=" + param.get("stnId"));
        urlBuilder.append("&" + URLEncoder.encode("tmFc","UTF-8") + "=" + param.get("tmFc"));
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
        log.info(sb.toString());
    }

    public HashMap<String,Object> callAPICovid(HashMap<String,Object> _param) throws IOException, Exception {
        HashMap<String,String> param = (HashMap<String, String>) _param.clone();

        if (commonUtil.isEmptyOrNull(param.get("base_date"))) {
            param.put("base_date", commonUtil.getMinusOneHour(commonUtil.getNow()).substring(0,8));
        }

        if (commonUtil.isEmptyOrNull(param.get("base_date"))) {
            param.put("base_date", commonUtil.getMinusOneHour(commonUtil.getNow()).substring(0,8));
        }

        StringBuilder urlBuilder = new StringBuilder("http://apis.data.go.kr/1352000/ODMS_COVID_04/callCovid04Api"); /*URL*/
        urlBuilder.append("?" + URLEncoder.encode("serviceKey","UTF-8") + "=서비스키"); /*Service Key*/
        urlBuilder.append("&" + URLEncoder.encode("pageNo","UTF-8") + "=" + URLEncoder.encode("1", "UTF-8")); /*페이지번호*/
        urlBuilder.append("&" + URLEncoder.encode("numOfRows","UTF-8") + "=" + URLEncoder.encode("500", "UTF-8")); /*한 페이지 결과 수*/
        urlBuilder.append("&" + URLEncoder.encode("apiType","UTF-8") + "=" + URLEncoder.encode("xml", "UTF-8")); /*결과형식(xml/json)*/
        urlBuilder.append("&" + URLEncoder.encode("std_day","UTF-8") + "=" + URLEncoder.encode("2021-12-15", "UTF-8")); /*기준일자*/

        //TODO 전국은 안돼????확인해봐야함
        urlBuilder.append("&" + URLEncoder.encode("gubun","UTF-8") + "=" + URLEncoder.encode("경기", "UTF-8")); /*시도명*/
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
        log.info(sb.toString());
        return objectMapper.readValue(sb.toString(), HashMap.class);
    }

    public ArrayList getItem(HashMap<String, Object> param) throws Exception {

        //[시작] depth1Response
        HashMap<String, Object> depth1Response = (HashMap<String, Object>) param.get("response");
        //[종료] depth1Response

        //[시작] depth2Body
        if (depth1Response.get("body") == null || "".equals(depth1Response.get("body"))) {
            throw new Exception(String.valueOf(Const.NO_DATA_API_ERROR));
        }
        HashMap<String, Object> depth2Body = (HashMap<String, Object>) depth1Response.get("body");
        //[종료] depth2Body

        //[시작] depth3Items
        if (depth2Body.get("items") == null || "".equals(depth2Body.get("items"))) {
            throw new Exception(Const.NO_DATA_API_ERROR_MSG);
        }
        HashMap<String, Object> depth3Items = (HashMap<String, Object>) depth2Body.get("items");
        //[종료] depth3Items

        //[시작] depth4Item
        ArrayList depth4Item = (ArrayList) depth3Items.get("item");
        //[종료] depth4Item
        return depth4Item;
    }
}
