package com.web.service;


import com.web.common.CommonError;
import com.web.common.CommonException;
import com.web.common.CommonResVO;
import com.web.common.DataGoAPI;
import com.web.dto.CovidDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Service
public class CovidService {

	@Autowired
	private DataGoAPI dataGoAPI;

	@Autowired
	private MongoTemplate mongoTemplate;


	public CommonResVO getCovidMongoDB(HashMap<String,String> param){
		CommonResVO result = new CommonResVO();
//        List<CovidVO> covidList = mongoTemplate.find(
//                new Query().with(Sort.by(Sort.Direction.DESC, "id"))
//                , CovidVO.class);

		List<CovidDto> covidList = mongoTemplate.find(
				new Query().limit(999999999).with(Sort.by(Sort.Direction.DESC, "_id"))
				, CovidDto.class);

		result.setResult(covidList);
		return result;
	}


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
				this.setCovidMongoDB(paramCovid);

				startCreateDt.delete(0, startCreateDt.length());
				endCreateDt.delete(0, endCreateDt.length());
			}
		}
		return true;
	}


	public CommonResVO setCovidMongoDB(HashMap<String,String> _param) throws Exception {
		CommonResVO result = new CommonResVO();

		try {
			HashMap<String, String> param = (HashMap<String, String>) _param.clone();
			HashMap<String, Object> covidResult = dataGoAPI.callGetCovid(param);

			ArrayList depth4Item = dataGoAPI.getItem(covidResult);

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

				mongoTemplate.save(new CovidDto(
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
}
