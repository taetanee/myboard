package com.web.controller;

import com.web.common.CommonResponse;
import com.web.service.CommonService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@Api(tags="테스트 컨트롤러")
@RestController
@RequestMapping("/test")
@Slf4j
public class TestRestController {

	@Autowired
	private CommonService commonService;

	@GetMapping("/getUuid")
	public ResponseEntity<?> getUuid(){
		CommonResponse response = new CommonResponse();
		HashMap<String,Object> result = commonService.getUuid();
		response.setResult(result);
		return ResponseEntity.ok(response);
	}
}
