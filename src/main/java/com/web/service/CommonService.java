package com.web.service;

import com.web.common.util.CommonUtil;
import com.web.mapper.CommonMapper;
import com.web.dto.CovidDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
@Slf4j
public class CommonService {

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private CommonMapper commonMapper;


    public HashMap<String,Object> getUuid(){
        HashMap<String,Object> result = new HashMap<>();
        result.put("result",commonUtil.getUUID());
        return result;
    }


    public CovidDto junitTest(){
        CovidDto result = new CovidDto();
        result.setUpdateDt("A");
        return result;
    }

    public HashMap<String,String> checkHealth() {
        return commonMapper.checkHealth();
    }
}