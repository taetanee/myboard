package com.web.controller;


import com.web.service.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.net.URLEncoder;
import java.util.HashMap;

@Slf4j
@Configuration
@EnableScheduling
public class SchedulerController {

    @Autowired
    private ServiceImpl service;

    public static void main(String args[]){

        StringBuffer startCreateDt = new StringBuffer();
        StringBuffer endCreateDt = new StringBuffer();

        for(int year=2021;year<=2022;year++){
            for(int month=01;month<=12;month++){

                StringBuffer tempDt = new StringBuffer();
                tempDt.append(year);
                tempDt.append(String.format("%02d", month));

                startCreateDt.append(tempDt);
                startCreateDt.append("01");

                endCreateDt.append(tempDt);
                endCreateDt.append("31");

                System.out.println(startCreateDt+"~"+endCreateDt);

                startCreateDt.delete(0, startCreateDt.length());
                endCreateDt.delete(0, endCreateDt.length());
            }
        }
    }

    //@Scheduled(cron = "* * * * * *")
    private boolean setCovid() throws Exception{
        HashMap<String,String> paramCovid = new HashMap();

        StringBuffer startCreateDt = new StringBuffer();
        StringBuffer endCreateDt = new StringBuffer();

        for(int year=2020;year<=2022;year++){
            for(int month=01;month<=12;month++){

                StringBuffer tempDt = new StringBuffer();
                tempDt.append(year);
                tempDt.append(String.format("%02d", month));

                startCreateDt.append(tempDt);
                startCreateDt.append("01");

                endCreateDt.append(tempDt);
                endCreateDt.append("31");

                if( true ){
                    paramCovid.put("startCreateDt", startCreateDt.toString());
                    paramCovid.put("endCreateDt", endCreateDt.toString());
                } else {
                    paramCovid.put("startCreateDt", "20220201");
                    paramCovid.put("endCreateDt", "20220231");
                }

                log.info("[startCreateDt] : " + startCreateDt + "[endCreateDt] : " + endCreateDt);
                service.setCovid(paramCovid);

                startCreateDt.delete(0, startCreateDt.length());
                endCreateDt.delete(0, endCreateDt.length());
            }
        }
        return true;
    }
}
