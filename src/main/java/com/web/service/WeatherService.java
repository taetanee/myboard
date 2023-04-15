package com.web.service;


import com.web.common.DataGoAPI;
import com.web.dto.CovidDto;
import com.web.dto.STWeatherDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;

@Slf4j
@Service
public class WeatherService {

	@Autowired
	private DataGoAPI dataGoAPI;

	@Autowired
	private MongoTemplate mongoTemplate;


	public HashMap<String,Object> getShortTermWeather(HashMap<String,Object> param) throws Exception{
		HashMap<String,Object> result = new HashMap<>();
		result = dataGoAPI.getShortTermWeather(param);
		this.setShortTermWeather(result);
		return result;
	}


	public void setShortTermWeather(HashMap<String, Object> param) throws Exception {
		ArrayList arrayList = dataGoAPI.getItem(param);
		for (int i = 0; i < arrayList.size(); i++) {
			HashMap rawData = (HashMap) arrayList.get(i);
			mongoTemplate.save(
					new STWeatherDTO(
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

}
