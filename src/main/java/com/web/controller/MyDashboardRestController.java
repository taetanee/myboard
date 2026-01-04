package com.web.controller;

import com.web.service.MyDashboardService;
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
@RequestMapping("/myDashboard")
@Slf4j
public class MyDashboardRestController {

	@Autowired
	private MyDashboardService myDashboardService;

	@GetMapping("/getMinuDustFrcstDspth")
	public ResponseEntity<String> getMinuDustFrcstDspth() throws Exception {
		String result = myDashboardService.getMinuDustFrcstDspth(new HashMap<>());
		return ResponseEntity.ok().contentType(MediaType.valueOf("application/json;charset=UTF-8")).body(result);
	}

	@GetMapping("/getSnp500CurrentPrice")
	public ResponseEntity<Map<String, Object>> getSnp500CurrentPrice() throws Exception {
		try {
			Map<String, Object> result = myDashboardService.getSnp500CurrentPrice();
			return ResponseEntity.ok(result);
		} catch (Exception e) {
			Map<String, Object> error = new HashMap<>();
			error.put("error", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
		}
	}

	@GetMapping("/getCurrentWeather")
	public ResponseEntity<Map<String, Object>> getCurrentWeather() {
		try {
			Map<String, Object> result = myDashboardService.getCurrentSeoulWeather();
			return ResponseEntity.ok(result);
		} catch (Exception e) {
			Map<String, Object> error = new HashMap<>();
			error.put("error", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
		}
	}

	@GetMapping("/getExchangeRateUSDToKRW")
	public ResponseEntity<Map<String, Object>> getExchangeRateUSDToKRW() throws Exception {
		try {
			Map<String, Object> result = myDashboardService.getExchangeRateUSDToKRW();
			return ResponseEntity.ok(result);
		} catch (Exception e) {
			Map<String, Object> error = new HashMap<>();
			error.put("error", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
		}
	}

	@GetMapping("/getFearAndGreedIndex")
	public ResponseEntity<Map<String, Object>> getFearAndGreedIndex() {
		try {
			Map<String, Object> result = myDashboardService.getFearAndGreedIndex();
			return ResponseEntity.ok(result);
		} catch (Exception e) {
			Map<String, Object> error = new HashMap<>();
			error.put("error", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
		}
	}

	@GetMapping("/getVixIndex")
	public ResponseEntity<Map<String, Object>> getVixIndex() {
		try {
			Map<String, Object> result = myDashboardService.getVixIndex();
			return ResponseEntity.ok(result);
		} catch (Exception e) {
			Map<String, Object> error = new HashMap<>();
			error.put("error", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
		}
	}



}
