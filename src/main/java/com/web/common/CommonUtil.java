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

    public boolean isEmptyOrNull(String str) {
        if (str != null && !str.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    //TODO : 일단 String 자료형으로만 했는데, 추후에 다른 자료형 필요할 수 있음
    public String getValueIfNull(String targetObject, String nullObject){
        String result = "";
        if(this.isEmptyOrNull(targetObject)){
            result = nullObject;
        } else {
            result = targetObject;
        }
        return result;
    }

    public String getNowDate() {
        return this.getNowDate("yyyyMMdd");
    }

    public String getNowDate(String pattern){
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        Calendar c1 = Calendar.getInstance();
        String strToday = sdf.format(c1.getTime());
        return strToday;
    }

    public String getUUID(){
        final int length = 8;
        return this.getUUID(length);
    }

    public String getUUID(int length){
        UUID result = UUID.randomUUID();
        return result.toString().substring(0, length);
    }
}
