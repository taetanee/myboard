package com.web.controller;

import com.web.common.CommonRes;
import com.web.service.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/")
public class AjaxController {

    @Autowired
    private ServiceImpl service;


    //uuid 가져오기
    @RequestMapping("/getUuid")
    public ResponseEntity<?> getUuid(){
        CommonRes response = new CommonRes();
        HashMap<String,Object> result = service.getUuid();
        response.setResult(result);
        return ResponseEntity.ok(response);
    }

    //날씨 가져오기
    @RequestMapping("/getWeather")
    public ResponseEntity<?> getWeather(HashMap<String,String> param){
        CommonRes response = new CommonRes();
        HashMap<Object,Object> result = service.getWeather(param);
        response.setResult(result);
        return ResponseEntity.ok(response);
    }

    //코로나 감염자 가져오기
    @RequestMapping("/getCovid")
    public ResponseEntity<?> getCovid(HashMap<String,String> param){
        CommonRes response = new CommonRes();
        HashMap<Object,Object> result = service.getCovid(param);
        response.setResult(result);
        return ResponseEntity.ok(response);
    }

}
