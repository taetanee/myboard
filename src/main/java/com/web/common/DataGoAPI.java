package com.web.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;

@Component
public class DataGoAPI {

    private static String serviceKey = "vvSbtDzTIbQ9rNkwq8WqL9SYwjihCcEujiNogCS9sgk37RU%2B3KJIRoQ6b%2FpY452SbKenj5A3RnPdgyup1jillw%3D%3D";

    private static String shortTermWeatherURL = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtNcst";

    private static String mediumTermWeatherURL = "http://apis.data.go.kr/1360000/MidFcstInfoService/getMidFcst";

    @Autowired
    private CommonUtil commonUtil;

    public static void main(String[] args) {
        DataGoAPI _this = new DataGoAPI();
        try {
            HashMap<String,String> param = new HashMap();
            _this.getShortTermWeather(param);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            HashMap<String,String> param = new HashMap();
            _this.getMediumTermWeather(param);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String getShortTermWeather(HashMap<String, String> _param) throws IOException {

        HashMap<String,String> param = (HashMap<String, String>) _param.clone();

        param.put("pageNo",URLEncoder.encode("1", "UTF-8"));  /*페이지번호*/
        param.put("numOfRows",URLEncoder.encode("1000", "UTF-8"));  /*한 페이지 결과 수*/
        param.put("dataType",URLEncoder.encode("XML", "UTF-8")); /*요청자료형식(XML/JSON) Default: XML*/
        param.put("base_date",URLEncoder.encode("20220206", "UTF-8")); /*‘21년 6월 28일 발표*/
        param.put("base_time",URLEncoder.encode("0800", "UTF-8")); /*06시 발표(정시단위) */
        param.put("nx",URLEncoder.encode("55", "UTF-8")); /*예보지점의 X 좌표값*/
        param.put("ny",URLEncoder.encode("127", "UTF-8")); /*예보지점의 Y 좌표값*/

        StringBuilder urlBuilder = new StringBuilder(shortTermWeatherURL);
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
        return sb.toString();
    }

    private void getMediumTermWeather(HashMap<String,String> _param) throws IOException {

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
    }
}
