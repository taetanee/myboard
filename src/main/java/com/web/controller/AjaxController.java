package com.web.controller;

import com.web.service.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/")
public class AjaxController {

    @Autowired
    private ServiceImpl service;


    @RequestMapping("/getWeather")
    public HashMap<Object,Object> getWeather(HashMap<String,String> param){
        HashMap<Object,Object> result = service.getWeather(param);
        return result;
    }

    @Deprecated
    @RequestMapping("/getQuestion")
    public List<HashMap<String,Object>> getQuestion(String q_uid){

        if(q_uid == null || q_uid == ""){
            q_uid = "Q0";
        }

        List<HashMap<String,Object>> result = service.getQuestion(q_uid);
        return result;
    }

    @Deprecated
    @RequestMapping("/getPreEquation")
    public HashMap<String,Object> getPreEquation(String e_uid){
        HashMap<String,Object> result = service.getPreEquation(e_uid);
        return result;
    }


}
