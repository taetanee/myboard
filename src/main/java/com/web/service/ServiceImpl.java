package com.web.service;

import com.web.common.*;
import com.web.mapper.TestMapper;
import com.web.vo.CovidVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
@Slf4j
public class ServiceImpl {

    @Autowired
    private DataGoAPI dataGoAPI;

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private TestMapper testMapper;

    @Autowired
    private MongoTemplate mongoTemplate;

    public HashMap<String,Object> getUuid(){
        HashMap<String,Object> result = new HashMap<>();
        result.put("result",commonUtil.getUUID());
        return result;
    }

    public CommonResVO getCovidMongoDB(HashMap<String,String> param){
        CommonResVO result = new CommonResVO();
//        List<CovidVO> covidList = mongoTemplate.find(
//                new Query().with(Sort.by(Sort.Direction.DESC, "id"))
//                , CovidVO.class);

        List<CovidVO> covidList = mongoTemplate.find(
                new Query().limit(999999999).with(Sort.by(Sort.Direction.DESC, "_id"))
                , CovidVO.class);

        result.setResult(covidList);
        return result;
    }

    public CommonResVO setCovidMongoDB(HashMap<String,String> _param) throws Exception {
        CommonResVO result = new CommonResVO();

        try {
            HashMap<String, String> param = (HashMap<String, String>) _param.clone();
            HashMap<String, Object> covidResult = dataGoAPI.callGetCovid(param);

            //[시작] depth1Response
            HashMap<String, Object> depth1Response = (HashMap<String, Object>) covidResult.get("response");
            //[종료] depth1Response

            //[시작] depth2Body
            if (depth1Response.get("body") == null || "".equals(depth1Response.get("body"))) {
                throw new CommonException(CommonError.COVID_RESULT_WARN);
            }
            HashMap<String, Object> depth2Body = (HashMap<String, Object>) depth1Response.get("body");
            //[종료] depth2Body

            //[시작] depth3Items
            HashMap<String, Object> depth3Items = (HashMap<String, Object>) depth2Body.get("items");
            //[종료] depth3Items

            //[시작] depth4Item
            ArrayList depth4Item = (ArrayList) depth3Items.get("item");
            //[종료] depth4Item

            for (int i = 0; i < depth4Item.size(); i++) {
                HashMap rawData = (HashMap) depth4Item.get(i);
                double accDefRate = 0;
                int accExamCnt = 0;
                if (rawData.get("accDefRate") == null) {
                    accDefRate = 0;
                } else if (rawData.get("accDefRate") instanceof Double) {
                    accDefRate = (double) rawData.get("accDefRate");
                } else if (rawData.get("accDefRate") instanceof Integer) {
                    accDefRate = (int) rawData.get("accDefRate");
                } else {
                    log.warn("accDefRate가 예측된 자료형이 아님");
                }

                if (rawData.get("accExamCnt") == null) {
                    accExamCnt = 0;
                } else if (rawData.get("accExamCnt") instanceof Integer) {
                    accExamCnt = (int) rawData.get("accExamCnt");
                } else {
                    log.warn("accExamCnt가 예측된 자료형이 아님");
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

        } catch (CommonException e) {
            log.warn("[CommonException발생] ERR_CODE : "+ e.getErrCode());
            for (String key : _param.keySet()) {
                System.out.println("key : " + key + "/" + "value : " + _param.get(key));
            }
        } catch (ClassCastException e) {
            log.warn("[ClassCastException] ERR_CODE : "+ e);
            for (String key : _param.keySet()) {
                System.out.println("key : " + key + "/" + "value : " + _param.get(key));
            }
        } catch (Exception e) {
            throw new Exception(e);
        }

        return result;
    }


    public HashMap<Object,Object> getShortTermWeather(HashMap<String,String> param){
        HashMap<Object,Object> result = new HashMap<>();
        try {
            result = dataGoAPI.getShortTermWeather(param);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }


    public CovidVO unitTest(){
        CovidVO result = new CovidVO();
        result.setUpdateDt("A");
        return result;
    }

    public HashMap<String,String> checkHealth() {
        return testMapper.checkHealth();
    }
}