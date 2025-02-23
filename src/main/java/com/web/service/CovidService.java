package com.web.service;


import com.google.gson.Gson;
import com.web.common.MyException;
import com.web.common.CommonResponse;
import com.web.common.util.APIUtil;
import com.web.common.util.CommonUtil;
import com.web.common.util.RedisUtil;
import com.web.dto.CovidDTO;
import com.web.dto.ShortWeatherDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class CovidService {

	@Autowired
	private APIUtil apiUtil;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private RedisUtil redisUtil;

	@Autowired
	private CommonUtil commonUtil;


	public ArrayList getCovid(HashMap<String,Object> _param) throws Exception{

		ArrayList result = new ArrayList();
		//[시작] 기본값 세팅
		HashMap<String,Object> param = (HashMap<String, Object>) _param.clone();
		if(param.get("startCreateDt") == null){
			param.put("startCreateDt", commonUtil.getNowDate());
		}
		if(param.get("endCreateDt") == null){
			param.put("endCreateDt", commonUtil.getNowDate());
		}
		String key = "covid" + "_" + param.get("startCreateDt") +"_" + param.get("endCreateDt");
		//[종료] 기본값 세팅

		//[시작] redis에서 데이터 가져오기
		Set redisData = redisUtil.getSets(key);
		if (false && redisData.size() != 0) {
			for( Object item :  redisData ){
				Gson gson = new Gson();
				CovidDTO jsonObject = gson.fromJson((String) item, CovidDTO.class);
				result.add(jsonObject);
			}
			return result;
		}
		//[종료] redis에서 데이터 가져오기

		//[시작] mongo에서 데이터 가져오기
		List<ShortWeatherDTO> mongoData = this.findMongoCovid(param);
		if (mongoData.size() != 0) {
			for(int i=0;i<mongoData.size();i++){
				ShortWeatherDTO item = mongoData.get(i);
				result.add(item);
			}
			return result;
		}
		//[종료] mongo에서 데이터 가져오기

		//[시작] API에서 데이터 가져오기
		HashMap<String,Object> apiDataTemp = apiUtil.callAPICovid(param);
		ArrayList apiData = apiUtil.getItem(apiDataTemp);
		//[종료] API에서 데이터 가져오기

		return result;
	}

	public List<ShortWeatherDTO> findMongoCovid(HashMap<String,Object> _param){
		HashMap<String,Object> param = (HashMap<String, Object>) _param.clone();

		Query query = new Query()
				.addCriteria(Criteria.where("startCreateDt").is(param.get("startCreateDt")))
				.addCriteria(Criteria.where("endCreateDt").is(param.get("endCreateDt")))
				.limit(999999999)
				.with(Sort.by(Sort.Direction.DESC, "_id"));

		List<ShortWeatherDTO> shortWeatherDTO = mongoTemplate.find(query.with(Sort.by(Sort.Direction.DESC, "_id")), ShortWeatherDTO.class);
		return shortWeatherDTO;
	}





	public CommonResponse setCovidMongoDB(HashMap<String,Object> _param) throws Exception {
		CommonResponse result = new CommonResponse();

		try {
			HashMap<String, Object> param = (HashMap<String, Object>) _param.clone();
			HashMap<String, Object> covidResult = apiUtil.callAPICovid(param);

			ArrayList depth4Item = apiUtil.getItem(covidResult);

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

				mongoTemplate.save(new CovidDTO(
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

		} catch (MyException e) {
			log.warn("[except발생] ERR_CODE : "+ e.getErrCode());
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
