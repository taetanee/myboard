package com.web.common;

import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;

@Component
public class CommonUtil {

    public static void main(String args[]){
        CommonUtil _this = new CommonUtil();
//        String result1 = _this.getNowDate("yyyyMMdd");
//        System.out.println(result1);

        System.out.println(_this.getUUID());
    }

    public boolean isEmptyOrNull(String str) {
        if (str != null && !str.isEmpty()) {
            return false;
        } else {
            return true;
        }
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
