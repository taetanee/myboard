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

    @Deprecated
    @Autowired
    private HouseMapper houseMapper;

    @Autowired
    private DataGoAPI dataGoAPI;

    @Autowired
    private CommonUtil commonUtil;

    @Deprecated
    public List<HashMap<String,Object>> getQuestion(String qUid){
        return houseMapper.getQuestion(qUid);
    }

    @Deprecated
    public HashMap<String,Object> getPreEquation(String eUid){
        return houseMapper.getPreEquation(eUid);
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

    public HashMap<String,Object> getUuid(){
        HashMap<String,Object> result = new HashMap<>();
        result.put("result",commonUtil.getUUID());
        return result;
    }
}
