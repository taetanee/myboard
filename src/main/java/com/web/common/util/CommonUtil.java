package com.web.common.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class CommonUtil {

    @Autowired
    private StringRedisTemplate redisTemplate;

    static public boolean isEmptyOrNull(String str) {
        if (str != null && !str.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    public String NVL(String targetObject, String nullObject){
        String result = "";
        if(this.isEmptyOrNull(targetObject)){
            result = nullObject;
        } else {
            result = targetObject;
        }
        return result;
    }

    public static String formatNow(String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            pattern = "yyyy-MM-dd HH:mm:ss"; // 기본값
        }
        return new SimpleDateFormat(pattern).format(new Date());
    }

    public String getUUID(){
        final int length = 8;
        return this.getUUID(length);
    }

    public String getUUID(int length){
        UUID result = UUID.randomUUID();
        return result.toString().substring(0, length);
    }

    public String getCache(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            return null;
        }
    }

    public void setCache(String key, String value, long timeoutSeconds) {
        try {
            if (value != null && !value.isEmpty()) {
                redisTemplate.opsForValue().set(key, value, timeoutSeconds, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            // 로깅 생략
        }
    }

}
