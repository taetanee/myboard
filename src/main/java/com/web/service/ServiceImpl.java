package com.web.service;

import com.web.common.CommonUtil;
import com.web.common.DataGoAPI;
import com.web.mapper.HouseMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

@Service
public class ServiceImpl {

    @Autowired
    private DataGoAPI dataGoAPI;

    @Autowired
    private CommonUtil commonUtil;

    public HashMap<String,Object> getUuid(){
        HashMap<String,Object> result = new HashMap<>();
        result.put("result",commonUtil.getUUID());
        return result;
    }

    public HashMap<String,Object> getCovid(HashMap<String,String> param){
        HashMap<String,Object> result = new HashMap<>();
        try {
            result = dataGoAPI.getCovid(param);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public HashMap<Object,Object> getWeather(HashMap<String,String> param){
        HashMap<Object,Object> result = new HashMap<>();
        try {
            result = dataGoAPI.getShortTermWeather(param);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
