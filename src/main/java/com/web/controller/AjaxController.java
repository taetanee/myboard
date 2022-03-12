package com.web.controller;

import com.web.common.CommonRes;
import com.web.service.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
@RequestMapping("/")
public class AjaxController {

    @Autowired
    private ServiceImpl service;


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

    //set 코로나
    @RequestMapping("/setCovid")
    public ResponseEntity<?> setCovid(HashMap<String,String> param){
        CommonRes response = new CommonRes();
        HashMap<String,Object> result = service.setCovid(param);
        response.setResult(result);
        return ResponseEntity.ok(response);
    }



}
