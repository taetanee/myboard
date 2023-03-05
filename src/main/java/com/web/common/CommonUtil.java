package com.web.common;

import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.UUID;

@Component
public class CommonUtil {

    public static void main(String args[]){
        CommonUtil _this = new CommonUtil();

        HashMap<String,String> param = new HashMap();
        param.put("xx","");
        System.out.println(_this.getValueIfNull(param.get("xx"),"isNull"));
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
    public String getValueIfNull(String targetObject, String nullObject){
        String result = "";
        if(this.isEmptyOrNull(targetObject)){
            result = nullObject;
        } else {
            result = targetObject;
        }
        return result;
    }

    /**
     * 현재시간을 yyyyMMdd로 반환하는 함수
     */
    public String getNowDate() {
        return this.getNowDate("yyyyMMdd");
    }

    /**
     * 현재시간을 첫번째 인자의 패턴으로 반환하는 함수
     * @param pattern 날짜 형식 패턴
     */
    public String getNowDate(String pattern){
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
}
