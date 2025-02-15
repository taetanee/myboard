package com.web.controller;

import com.web.common.CommonResVO;
import com.web.common.MyException;
import com.web.service.OnlineClipboardService;
import com.web.service.WeatherService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;

@Api(tags="날씨 컨트롤러")
@RestController
@RequestMapping("/onlineClipboard")
@Slf4j
public class OnlineClipboardController {

	@Autowired
	private OnlineClipboardService onlineClipboardService;

	@PostMapping("/getRandomWord")
	public ResponseEntity<?> getRandomWord() throws Exception {
		CommonResVO response = new CommonResVO();
		String result = null;
		try {
			result = onlineClipboardService.getRandomWord();
		} catch (MyException e) {
			throw new MyException(e);
		} catch (Exception e) {
			throw new Exception(e);
		}

		response.setResult(result);
		return ResponseEntity.ok(response);
	}
}
