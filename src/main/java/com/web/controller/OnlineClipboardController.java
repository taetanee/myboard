package com.web.controller;

import com.web.common.CommonResponse;
import com.web.common.MyException;
import com.web.service.OnlineClipboardService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
		CommonResponse response = new CommonResponse();
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

	@PostMapping("/saveContent")
	public ResponseEntity<?> saveContent(@RequestBody HashMap<String, Object> param) throws Exception {
		CommonResponse response = new CommonResponse();
		String result = null;
		try {
			result = onlineClipboardService.saveContent(param);
		} catch (MyException e) {
			throw new MyException(e);
		} catch (Exception e) {
			throw new Exception(e);
		}

		response.setResult(result);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/getContent")
	public ResponseEntity<?> getContent(@RequestBody HashMap<Object, Object> param) throws Exception {
		CommonResponse response = new CommonResponse();
		HashMap<Object,Object> result = null;
		try {
			result = onlineClipboardService.getContent(param);
		} catch (MyException e) {
			throw new MyException(e);
		} catch (Exception e) {
			throw new Exception(e);
		}
		response.setResult(result);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/upload")
	public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile param) throws Exception {
		CommonResponse response = new CommonResponse();
		HashMap<Object,Object> result = null;
		try {
			result = onlineClipboardService.uploadFile(param);
		} catch (MyException e) {
			throw new MyException(e);
		} catch (Exception e) {
			throw new Exception(e);
		}
		response.setResult(result);
		return ResponseEntity.ok(response);
	}


	@GetMapping("/download")
	public ResponseEntity<Resource> downloadFile(@RequestParam String fileName) throws Exception {
		CommonResponse response = new CommonResponse();
		ResponseEntity<Resource> result = null;
		return onlineClipboardService.downloadFile(fileName);
	}
}
