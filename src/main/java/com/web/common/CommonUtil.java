package com.web.common;

import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Calendar;

@Component
public class CommonUtil {

    public static void main(String args[]){
        CommonUtil _this = new CommonUtil();
        String result1 = _this.getDate("yyyyMMdd");
        System.out.println(result1);
    }

    public boolean isEmptyOrNull(String str) {
        if (str != null && !str.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    public String getDate() {
        return this.getDate("yyyyMMdd");
    }

    public String getDate(String pattern){
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        Calendar c1 = Calendar.getInstance();
        String strToday = sdf.format(c1.getTime());
        return strToday;
    }
}
