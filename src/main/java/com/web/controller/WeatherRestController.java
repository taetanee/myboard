package com.web.controller;

import com.web.service.WeatherService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

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
}
