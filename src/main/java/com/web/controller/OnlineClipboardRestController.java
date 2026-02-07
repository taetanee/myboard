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
import java.util.List;

@Api(tags="온라인 클립보드")
@RestController
@RequestMapping("/onlineClipboard")
@Slf4j
public class OnlineClipboardRestController {

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
	public ResponseEntity<?> uploadFile(@RequestPart("file") MultipartFile param, @RequestPart("randomWord") String randomWord) throws Exception {
		CommonResponse response = new CommonResponse();
		HashMap<Object,Object> result = null;
		try {
			result = onlineClipboardService.uploadFile(param, randomWord);
		} catch (MyException e) {
			throw new MyException(e);
		} catch (Exception e) {
			throw new Exception(e);
		}
		response.setResult(result);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/download")
	public ResponseEntity<Resource> downloadFile(@RequestParam String randomWord, @RequestParam String fileName) throws Exception {
		return onlineClipboardService.downloadFile(randomWord, fileName);
	}

	@DeleteMapping("/deleteFile")
	public ResponseEntity<?> deleteFile(@RequestParam String randomWord, @RequestParam String fileName) throws Exception {
		CommonResponse response = new CommonResponse();
		try {
			onlineClipboardService.deleteFile(randomWord, fileName);
		} catch (MyException e) {
			throw new MyException(e);
		} catch (Exception e) {
			throw new Exception(e);
		}
		return ResponseEntity.ok(response);
	}

	@GetMapping("/fileList")
	public ResponseEntity<?> getFileList(@RequestParam String randomWord) throws Exception {
		CommonResponse response = new CommonResponse();
		List<String> result = null;
		try {
			result = onlineClipboardService.getFileList(randomWord);
		} catch (MyException e) {
			throw new MyException(e);
		} catch (Exception e) {
			throw new Exception(e);
		}
		response.setResult(result);
		return ResponseEntity.ok(response);
	}
}
