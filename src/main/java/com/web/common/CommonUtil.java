package com.web.common;

import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.UUID;

@Component
public class CommonUtil {

    public static void main(String args[]){
        CommonUtil _this = new CommonUtil();

        HashMap<String,String> param = new HashMap();
        param.put("xx","");
        System.out.println(_this.NVL(param.get("xx"),"isNull"));
    }

    /**
     * String이 null이거나 빈 값일대 true를 반환하는 함수
     * @param str 체크하려는 값
     */
    static public boolean isEmptyOrNull(String str) {
        if (str != null && !str.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * String이 null이거나 빈 값일대 두번째 인자로 반환하는 함수
     * @param targetObject 체크하려는 값
     * @param nullObject null일때 반환하는 값
     */
    public String NVL(String targetObject, String nullObject){
        String result = "";
        if(this.isEmptyOrNull(targetObject)){
            result = nullObject;
        } else {
            result = targetObject;
        }
        return result;
    }

    /**
     * 현재시간을 yyyyMMdd HHmmss로 반환하는 함수
     */
    public String getNowDateTime() {
        return this.getNow("yyyyMMdd HHmmss");
    }

    /**
     * 현재시간을 yyyyMMdd로 반환하는 함수
     */
    public static String getNowDate() {
        return getNow("yyyyMMdd");
    }

    /**
     * 현재시간을 yyyyMMdd로 반환하는 함수
     */
    public String getNowTime() {
        return this.getNow("HH");
    }
    /*
     * 현재시간을 첫번째 인자의 패턴으로 반환하는 함수
     * @param pattern 날짜 형식 패턴
     */
    public String getNow(){
        return this.getNow("yyyyMMdd HHmmss");
    }


    /**
     * 현재시간을 첫번째 인자의 패턴으로 반환하는 함수
     * @param pattern 날짜 형식 패턴
     */
    public static String getNow(String pattern){
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        Calendar c1 = Calendar.getInstance();
        String strToday = sdf.format(c1.getTime());
        return strToday;
    }

    /**
     * UUID 생성 함수(default)
     */
    public String getUUID(){
        final int length = 8;
        return this.getUUID(length);
    }

    /**
     * UUID 생성 함수(파라미터에 의해 생성)
     */
    public String getUUID(int length){
        UUID result = UUID.randomUUID();
        return result.toString().substring(0, length);
    }

    /**
     * 입력받은 시간에서 1시간을 뺀 시간을 구하여 문자열로 반환하는 함수.
     * @param inputTime 입력받은 시간(형식: yyyyMMdd HHmmss)
     * @return 입력받은 시간에서 1시간을 뺀 시간(형식: yyyyMMdd HHmmss)
     */
    public static String getMinusOneHour(String inputTime) {
        // 입력받은 문자열을 LocalDateTime으로 변환
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd HHmmss");
        LocalDateTime localDateTime = LocalDateTime.parse(inputTime, formatter);

        // 1시간을 뺀 시간을 구함
        LocalDateTime modifiedTime = localDateTime.minusHours(1);

        // 변환할 형식 지정
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyyMMdd HHmmss");

        // 변환된 문자열 반환
        return modifiedTime.format(outputFormatter);
    }

}
