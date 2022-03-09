package com.web.controller;

import com.web.common.CommonRes;
import com.web.vo.Covid;
import com.web.service.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;

@RestController
@RequestMapping("/")
public class AjaxController {

    @Autowired
    private ServiceImpl service;

    @Autowired
    private MongoTemplate mongoTemplate;


    //get uuid
    @RequestMapping("/getUuid")
    public ResponseEntity<?> getUuid(){
        CommonRes response = new CommonRes();
        HashMap<String,Object> result = service.getUuid();
        response.setResult(result);
        return ResponseEntity.ok(response);
    }

    //get 날씨
    @RequestMapping("/getWeather")
    public ResponseEntity<?> getWeather(HashMap<String,String> param){
        CommonRes response = new CommonRes();
        HashMap<Object,Object> result = service.getWeather(param);
        response.setResult(result);
        return ResponseEntity.ok(response);
    }

    //set 코로나 감염자
    @RequestMapping("/setCovid")
    public ResponseEntity<?> setCovid(HashMap<String,String> param){
        CommonRes response = new CommonRes();
        HashMap<String,Object> result = service.getCovid(param);
        response.setResult(result);

        HashMap<String,Object> temp1;
        temp1 = ((HashMap<String, Object>) result.get("response"));
        HashMap<String,Object> temp2;
        temp2 = ((HashMap<String, Object>) temp1.get("body"));
        HashMap<String,Object> temp3;
        temp3 = (HashMap<String, Object>) temp2.get("items");
        ArrayList temp4;
        temp4 = (ArrayList) temp3.get("item");

        for(int i=0; i<temp4.size(); i++){
            HashMap rawData = (HashMap) temp4.get(i);
            mongoTemplate.save(new Covid(
                    (double) rawData.get("accDefRate")
                    , (int) rawData.get("accExamCnt")
                    , (String) rawData.get("stateTime")
                    , (int) rawData.get("deathCnt")
                    , (int) rawData.get("decideCnt")
                    , (int) rawData.get("stateDt")
                    , (String) rawData.get("updateDt")
                    , (String) rawData.get("createDt")
                    , (int) rawData.get("seq")
                    )
            );
        }
        return ResponseEntity.ok(response);
    }



}
