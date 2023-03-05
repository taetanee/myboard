package com.web.controller;

import com.web.service.CovidService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@Api(tags="코로나 컨트롤러")
@RestController
@RequestMapping("/covid")
@Slf4j
public class CovidRestController {

	@Autowired
	private CovidService covidService;

	@PostMapping("/getCovid")
	public ResponseEntity<?> getCovid(HashMap<String,String> param){
		return ResponseEntity.ok(covidService.getCovidMongoDB(param));
	}

}
