package com.web.controller;

import com.web.vo.CommonResVO;
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
        CommonResVO response = new CommonResVO();
        HashMap<String,Object> result = service.getUuid();
        response.setResult(result);
        return ResponseEntity.ok(response);
    }

    //get 날씨
    @RequestMapping("/getWeather")
    public ResponseEntity<?> getWeather(HashMap<String,String> param){
        CommonResVO response = new CommonResVO();
        HashMap<Object,Object> result = service.getWeather(param);
        response.setResult(result);
        return ResponseEntity.ok(response);
    }

    //set 코로나
    @RequestMapping("/getCovid")
    public ResponseEntity<?> getCovid(HashMap<String,String> param){
        return ResponseEntity.ok(service.getCovid(param));
    }

}
