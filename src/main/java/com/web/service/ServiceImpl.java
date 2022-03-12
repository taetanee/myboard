package com.web.service;

import com.web.common.CommonUtil;
import com.web.common.DataGoAPI;
import com.web.vo.CommonResVO;
import com.web.vo.CovidVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

@Service
public class ServiceImpl {

    @Autowired
    private DataGoAPI dataGoAPI;

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private MongoTemplate mongoTemplate;

    public HashMap<String,Object> getUuid(){
        HashMap<String,Object> result = new HashMap<>();
        result.put("result",commonUtil.getUUID());
        return result;
    }

    public HashMap<String,Object> getCovid(HashMap<String,String> param){
        HashMap<String,Object> result = new HashMap<>();
        try {
            result = dataGoAPI.getCovid(param);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public CommonResVO setCovid(HashMap<String,String> _param){
        CommonResVO result = new CommonResVO();

        try{
            HashMap<String,String> param = (HashMap<String, String>) _param.clone();
            HashMap<String,Object> covidResult = dataGoAPI.getCovid(param);

            //[시작]TODO 추후에 소스 개선
            HashMap<String,Object> temp1;
            temp1 = ((HashMap<String, Object>) covidResult.get("response"));
            HashMap<String,Object> temp2;
            temp2 = ((HashMap<String, Object>) temp1.get("body"));
            HashMap<String,Object> temp3;
            temp3 = (HashMap<String, Object>) temp2.get("items");
            ArrayList temp4;
            temp4 = (ArrayList) temp3.get("item");
            //[종료]TODO 추후에 소스 개선

            for(int i=0; i<temp4.size(); i++){
                HashMap rawData = (HashMap) temp4.get(i);
                double accDefRate = 0;
                int accExamCnt = 0;
                if(rawData.get("accDefRate") == null){
                    accDefRate = 0;
                } else if(rawData.get("accDefRate") instanceof Double){
                    accDefRate = (double) rawData.get("accDefRate");
                } else if(rawData.get("accDefRate") instanceof Integer){
                    accDefRate = (int) rawData.get("accDefRate");
                } else {
                    System.out.println("[경고] accDefRate가 예측된 자료형이 아님");
                }

                if(rawData.get("accExamCnt") == null){
                    accExamCnt = 0;
                } else if(rawData.get("accExamCnt") instanceof Integer){
                    accExamCnt = (int) rawData.get("accExamCnt");
                } else {
                    System.out.println("[경고] accExamCnt가 예측된 자료형이 아님");
                }

                mongoTemplate.save(new CovidVO(
                                accDefRate
                                , accExamCnt
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
        } catch (Exception e){
            e.printStackTrace();
            result.setResultCode("-999");
            result.setResultMsg("에러발생");
            return result;
        }

        return result;
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
}
