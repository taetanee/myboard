package com.web.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.web.common.DataGoAPI;
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
	private DataGoAPI dataGoAPI;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private RedisUtil redisUtil;

	@Autowired
	private ObjectMapper objectMapper;


	public ArrayList getShortWeather(HashMap<String,Object> param) throws Exception{
		ArrayList result = new ArrayList();

		//[시작] redis에서 데이터 가져오기
		Set redisData = redisUtil.getSets("shortWeather" + "_20230415" + "_127" + "_55");
		if (redisData.size() != 0) {
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
			//result = mongoData; TODO : 데이터 바꿔서 return
			//return result;
		}
		//[종료] mongo에서 데이터 가져오기

		//[시작] API에서 데이터 가져오기
		HashMap<String,Object> apiDataTemp = dataGoAPI.callAPIShortWeather(param);
		ArrayList apiData = dataGoAPI.getItem(apiDataTemp);
		//[종료] API에서 데이터 가져오기

		//[시작] save redis
		if (redisData.size() == 0) {
			this.saveRedisShortWeather(apiData);
		}
		//[종료] save redis

		//[시작] save mongo
		if (mongoData.size() != 0) {
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

	public List<ShortWeatherDTO> findMongoShortWeather(HashMap<String,Object> param){
		Query query = new Query()
				.addCriteria(Criteria.where("baseDate").is("20230415"))
				.addCriteria(Criteria.where("baseTime").is("2100"))
				.addCriteria(Criteria.where("category").is("RN1"))
				.addCriteria(Criteria.where("nx").is(55))
				.addCriteria(Criteria.where("ny").is(127))
				.limit(999999999)
				.with(Sort.by(Sort.Direction.DESC, "_id"));

		List<ShortWeatherDTO> shortWeatherDTO = mongoTemplate.find(query.with(Sort.by(Sort.Direction.DESC, "_id")), ShortWeatherDTO.class);
		return shortWeatherDTO;
	}


	public void saveRedisShortWeather(ArrayList arrayList) throws Exception {
		for (int i = 0; i < arrayList.size(); i++) {
			HashMap rawData = (HashMap) arrayList.get(i);
			redisUtil.setSets("shortWeather" + "_20230415" + "_127" + "_55", objectMapper.writeValueAsString(rawData));
		}
	}

}
