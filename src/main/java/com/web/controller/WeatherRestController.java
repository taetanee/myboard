package com.web.controller;

import com.web.common.CommonResVO;
import com.web.service.CommonService;
import com.web.service.WeatherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
@RequestMapping("/")
@Slf4j
public class WeatherRestController {

	@Autowired
	private WeatherService weatherService;


	@RequestMapping("/getShortTermWeather")
	public ResponseEntity<?> getShortTermWeather(HashMap<String,String> param){
		CommonResVO response = new CommonResVO();
		HashMap<Object,Object> result = weatherService.getShortTermWeather(param);
		response.setResult(result);
		return ResponseEntity.ok(response);
	}
}
