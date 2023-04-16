package com.web.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.web.common.util.APIUtil;
import com.web.common.Const;
import com.web.common.util.CommonUtil;
import com.web.common.util.RedisUtil;
import com.web.dto.ShortWeatherDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class WeatherService {

	@Autowired
	private APIUtil apiUtil;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private RedisUtil redisUtil;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private CommonUtil commonUtil;


	public ArrayList getShortWeather(HashMap<String,Object> _param) throws Exception{
		ArrayList result = new ArrayList();

		//[시작] 기본값 세팅
		HashMap<String,Object> param = (HashMap<String, Object>) _param.clone();
		if(param.get("nx") == null || param.get("ny") == null){
			param.put("nx", Const.NX);
			param.put("ny", Const.NY);
		}
		if(param.get("base_date") == null){
			param.put("baseDate", commonUtil.getMinusOneHour(commonUtil.getNow()).substring(0,8));
		}
		if(param.get("base_time") == null){
			param.put("baseTime", commonUtil.getMinusOneHour(commonUtil.getNow()).substring(9,11) + "00");
		}
		String key = "shortWeather" + "_" + param.get("baseDate") +"_" + param.get("baseTime") + "_" + param.get("nx") + "_" + param.get("ny");
		//[종료] 기본값 세팅

		//[시작] redis에서 데이터 가져오기
		Set redisData = redisUtil.getSets(key);
		if (false && redisData.size() != 0) {
			for( Object item :  redisData ){
				Gson gson = new Gson();
				ShortWeatherDTO jsonObject = gson.fromJson((String) item, ShortWeatherDTO.class);
				result.add(jsonObject);
			}
			return result;
		}
		//[종료] redis에서 데이터 가져오기

		//[시작] mongo에서 데이터 가져오기
		List<ShortWeatherDTO> mongoData = this.findMongoShortWeather(param);
		if (mongoData.size() != 0) {
			for(int i=0;i<mongoData.size();i++){
				ShortWeatherDTO item = mongoData.get(i);
				result.add(item);
			}
			return result;
		}
		//[종료] mongo에서 데이터 가져오기

		//[시작] API에서 데이터 가져오기
		HashMap<String,Object> apiDataTemp = apiUtil.callAPIShortWeather(param);
		ArrayList apiData = apiUtil.getItem(apiDataTemp);
		//[종료] API에서 데이터 가져오기

		//[시작] save redis
		if (redisData.size() == 0) {
			this.saveRedisShortWeather(key, apiData);
		}
		//[종료] save redis

		//[시작] save mongo
		if (mongoData.size() == 0) {
			this.saveMongoShortWeather(apiData);
		}
		//[종료] save mongo

		return apiData;
	}


	public void saveMongoShortWeather(ArrayList arrayList) throws Exception {
		for (int i = 0; i < arrayList.size(); i++) {
			HashMap rawData = (HashMap) arrayList.get(i);
			mongoTemplate.save(
					new ShortWeatherDTO(
							(String) rawData.get("baseDate")
							, (String) rawData.get("baseTime")
							, (String) rawData.get("category")
							, (int) rawData.get("nx")
							, (int) rawData.get("ny")
							, (String) rawData.get("obsrValue")
					)
			);
		}
	}

	public List<ShortWeatherDTO> findMongoShortWeather(HashMap<String,Object> _param){
		HashMap<String,Object> param = (HashMap<String, Object>) _param.clone();

		Query query = new Query()
				.addCriteria(Criteria.where("baseDate").is(param.get("baseDate")))
				.addCriteria(Criteria.where("baseTime").is(param.get("baseTime")))
				//.addCriteria(Criteria.where("category").is("RN1"))
				.addCriteria(Criteria.where("nx").is(param.get("nx")))
				.addCriteria(Criteria.where("ny").is(param.get("ny")))
				.limit(999999999)
				.with(Sort.by(Sort.Direction.DESC, "_id"));

		List<ShortWeatherDTO> shortWeatherDTO = mongoTemplate.find(query.with(Sort.by(Sort.Direction.DESC, "_id")), ShortWeatherDTO.class);
		return shortWeatherDTO;
	}


	public void saveRedisShortWeather(String key, ArrayList arrayList) throws Exception {
		for (int i = 0; i < arrayList.size(); i++) {
			HashMap rawData = (HashMap) arrayList.get(i);
			redisUtil.setSets(key, objectMapper.writeValueAsString(rawData));
		}
	}

}
