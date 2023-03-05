package com.web.controller;

import com.web.service.CovidService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
@RequestMapping("/covid")
@Slf4j
public class CovidRestController {

	@Autowired
	private CovidService covidService;

	@RequestMapping("/getCovid")
	public ResponseEntity<?> getCovid(HashMap<String,String> param){
		return ResponseEntity.ok(covidService.getCovidMongoDB(param));
	}

}
