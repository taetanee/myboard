package com.web.common;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.HashMap;

@Component
public class DataGoAPI {

    private static String serviceKey = "vvSbtDzTIbQ9rNkwq8WqL9SYwjihCcEujiNogCS9sgk37RU%2B3KJIRoQ6b%2FpY452SbKenj5A3RnPdgyup1jillw%3D%3D";

    private static String shortTermWeatherURL = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtNcst";

    private static String mediumTermWeatherURL = "http://apis.data.go.kr/1360000/MidFcstInfoService/getMidFcst";

    private static String covidURL = "http://openapi.data.go.kr/openapi/service/rest/Covid19/getCovid19InfStateJson";

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private ObjectMapper objectMapper;

    public static void main(String[] args) {
        DataGoAPI _this = new DataGoAPI();
//        try {
//            HashMap<String,String> param = new HashMap();
//            _this.getShortTermWeat0her(param);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

//        try {
//            HashMap<String,String> param = new HashMap();
//            _this.getMediumTermWeather(param);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

//        try {
//            HashMap<String,String> param = new HashMap();
//            _this.getCovid(param);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

    }

    public HashMap<Object,Object> getShortTermWeather(HashMap<String, String> _param) throws IOException {

        HashMap<String,String> param = (HashMap<String, String>) _param.clone();
        if( commonUtil.isEmptyOrNull(param.get("base_date")) ){
            param.put("base_date",commonUtil.getNowDate());
        }

        param.put("pageNo",URLEncoder.encode("1", "UTF-8"));  /*???????????????*/
        param.put("numOfRows",URLEncoder.encode("1000", "UTF-8"));  /*??? ????????? ?????? ???*/
        param.put("dataType",URLEncoder.encode("JSON", "UTF-8")); /*??????????????????(XML/JSON) Default: XML*/
        param.put("base_date",URLEncoder.encode(param.get("base_date"), "UTF-8")); /*???21??? 6??? 28??? ??????*/
        param.put("base_time",URLEncoder.encode("0800", "UTF-8")); /*06??? ??????(????????????) */
        param.put("nx",URLEncoder.encode("55", "UTF-8")); /*??????????????? X ?????????*/
        param.put("ny",URLEncoder.encode("127", "UTF-8")); /*??????????????? Y ?????????*/

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
        //objectMapper.readValue(sb.toString(), HashMap.class);
        return objectMapper.readValue(sb.toString(), HashMap.class);
    }

    private void getMediumTermWeather(HashMap<String,String> _param) throws IOException {

        HashMap<String,String> param = (HashMap<String, String>) _param.clone();
        param.put("pageNo",URLEncoder.encode("1", "UTF-8"));  /*???????????????*/
        param.put("numOfRows",URLEncoder.encode("10", "UTF-8"));  /*??? ????????? ?????? ???*/
        param.put("dataType",URLEncoder.encode("XML", "UTF-8")); /*??????????????????(XML/JSON) Default: XML*/
        param.put("stnId",URLEncoder.encode("108", "UTF-8")); /*108 ??????, 109 ??????, ??????, ????????? ??? (??????????????? ?????? ???????????? ??????)*/
        param.put("tmFc",URLEncoder.encode("202202060600", "UTF-8")); /*-??? 2???(06:00,18:00)??? ?????? ?????? ??????????????? ?????? YYYYMMDD0600 (1800)-?????? 24?????? ????????? ??????*/

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

    public HashMap<String,Object> getCovid(HashMap<String,String> _param) throws IOException {
        HashMap<String,String> param = (HashMap<String, String>) _param.clone();

        if(commonUtil.isEmptyOrNull(param.get("startCreateDt"))){
            System.out.println("startCreateDt isEmptyOrNull");
            //TODO exception??? ???????????? ?????????????????????
        }

        if(commonUtil.isEmptyOrNull(param.get("endCreateDt"))){
            System.out.println("endCreateDt isEmptyOrNull");
            //TODO exception??? ???????????? ?????????????????????
        }

        HashMap<String,String> paramAPI = new HashMap();
        paramAPI.put("pageNo",URLEncoder.encode(commonUtil.getValueIfNull(param.get("pageNo"),"1"), "UTF-8"));
        paramAPI.put("numOfRows",URLEncoder.encode(commonUtil.getValueIfNull(param.get("numOfRows"),"10"), "UTF-8"));
        paramAPI.put("dataType",URLEncoder.encode("JSON", "UTF-8"));
        paramAPI.put("startCreateDt",URLEncoder.encode(commonUtil.getValueIfNull(param.get("startCreateDt"),"20200101"), "UTF-8"));
        paramAPI.put("endCreateDt",URLEncoder.encode(commonUtil.getValueIfNull(param.get("endCreateDt"),"20200131"), "UTF-8"));

        StringBuilder urlBuilder = new StringBuilder(covidURL); /*URL*/
        urlBuilder.append("?" + URLEncoder.encode("serviceKey","UTF-8") + "="+serviceKey);
        urlBuilder.append("&" + URLEncoder.encode("pageNo","UTF-8") + "=" + paramAPI.get("pageNo"));
        urlBuilder.append("&" + URLEncoder.encode("numOfRows","UTF-8") + "=" + paramAPI.get("numOfRows"));
        urlBuilder.append("&" + URLEncoder.encode("startCreateDt","UTF-8") + "=" + paramAPI.get("startCreateDt"));
        urlBuilder.append("&" + URLEncoder.encode("endCreateDt","UTF-8") + "=" + paramAPI.get("endCreateDt"));
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
        JSONObject json = XML.toJSONObject(sb.toString());
        return objectMapper.readValue(json.toString(), HashMap.class);
    }
}
