package com.web.controller;


import com.web.service.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.net.URLEncoder;
import java.util.HashMap;

@Configuration
@EnableScheduling
public class SchedulerController {

    @Autowired
    private ServiceImpl service;

    //@Scheduled(cron = "* * * * * *")
    private boolean setCovid(){
        HashMap<String,String> param = new HashMap();
        param.put("startCreateDt", "20210110");
        param.put("endCreateDt", "20210130");
        service.setCovid(param);
        return true;
    }
}
