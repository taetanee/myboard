package com.web.controller;

import com.web.service.WeatherService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Api(tags="날씨 컨트롤러")
@RestController
@RequestMapping("/weather")
@Slf4j
public class WeatherRestController {

	@Autowired
	private WeatherService weatherService;

	@GetMapping("/getMinuDustFrcstDspth")
	public ResponseEntity<String> getMinuDustFrcstDspth() throws Exception {
		String result = weatherService.getMinuDustFrcstDspth(new HashMap<>());
		return ResponseEntity.ok().contentType(MediaType.valueOf("application/json;charset=UTF-8")).body(result);
	}

	@GetMapping("/getSnp500CurrentPrice")
	public ResponseEntity<String> getSnp500CurrentPrice() throws Exception {
		double result = weatherService.getSnp500CurrentPrice();
		return ResponseEntity.ok().contentType(MediaType.valueOf("application/json;charset=UTF-8")).body(String.valueOf(result));
	}

	@GetMapping("/getCurrentWeather")
	public ResponseEntity<Map<String, Object>> getCurrentWeather() {
		try {
			Map<String, Object> result = weatherService.getCurrentSeoulWeather();
			return ResponseEntity.ok(result);
		} catch (Exception e) {
			Map<String, Object> error = new HashMap<>();
			error.put("error", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
		}
	}

	@GetMapping("/getExchangeRateUSDToKRW")
	public ResponseEntity<String> getExchangeRateUSDToKRW() throws Exception {
		double result = weatherService.getExchangeRateUSDToKRW();
		return ResponseEntity.ok().contentType(MediaType.valueOf("application/json;charset=UTF-8")).body(String.valueOf(result));
	}



}
