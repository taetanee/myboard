package com.web.service;

import com.web.mapper.HouseMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
public class ServiceImpl {

    @Autowired
    private HouseMapper houseMapper;

    public List<HashMap<String,Object>> getQuestion(String qUid){
        return houseMapper.getQuestion(qUid);
    }

    public HashMap<String,Object> getPreEquation(String eUid){
        return houseMapper.getPreEquation(eUid);
    }
}
