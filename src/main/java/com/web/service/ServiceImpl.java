package com.web.service;

import com.web.common.CommonUtil;
import com.web.common.DataGoAPI;
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

    public HashMap<String,Object> setCovid(HashMap<String,String> _param){
        HashMap<String,String> param = (HashMap<String, String>) _param.clone();
        HashMap<String,Object> result = this.getCovid(param);

        //[시작]TODO 추후에 소스 개선
        HashMap<String,Object> temp1;
        temp1 = ((HashMap<String, Object>) result.get("response"));
        HashMap<String,Object> temp2;
        temp2 = ((HashMap<String, Object>) temp1.get("body"));
        HashMap<String,Object> temp3;
        temp3 = (HashMap<String, Object>) temp2.get("items");
        ArrayList temp4;
        temp4 = (ArrayList) temp3.get("item");
        //[종료]TODO 추후에 소스 개선

        for(int i=0; i<temp4.size(); i++){
            HashMap rawData = (HashMap) temp4.get(i);
            mongoTemplate.save(new CovidVO(
                    //[시작] TODO 더 근본적인 방법 없는지 고려해보기
                    rawData.get("accDefRate") instanceof Double ? (double) rawData.get("accDefRate") : (int) rawData.get("accDefRate")
                    //[종료] TODO 더 근본적인 방법 없는지 고려해보기
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
