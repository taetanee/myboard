package com.web.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.web.common.Const;
import com.web.common.MyException;
import com.web.common.util.CommonUtil;
import com.web.common.util.RedisUtil;
import com.web.mapper.OnlineClipboardMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

@Slf4j
@Service
public class OnlineClipboardService {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private CommonUtil commonUtil;

	@Autowired
	private OnlineClipboardMapper onlineClipboardMapper;

	@Autowired
	private RedisUtil redisUtil;

	private static final String UPLOAD_DIR = "C:/uploads/";

	public String getRandomWord() throws Exception {
		String result = new String();
		result = commonUtil.getUUID(3);
		return result;
	}

	public String saveContent(HashMap<String,Object> param) throws Exception {
		String result = new String();
		String keyContent = "content";
		String dataContent = (String) param.get(keyContent);

		String keyUrl = "randomWord";
		String urlData = (String) param.get(keyUrl);

		if (dataContent == null ){
			throw new MyException(Const.NOT_INVALID_PARAM_ERROR);
		}
		//redisUtil.setSets(urlData, objectMapper.writeValueAsString(dataContent));
		redisUtil.setValues(urlData, dataContent);
 		return result;
	}

	public HashMap<Object,Object> getContent(HashMap<Object,Object> param) throws Exception {
		HashMap<Object,Object> result = new HashMap<>();
		String resultx = new String();

		String keyUrl = "randomWord";
		String urlData = (String) param.get(keyUrl);

		resultx = redisUtil.getValues(urlData);
		result.put("data",resultx);
		return result;
	}


	public HashMap<Object,Object> uploadFile(MultipartFile param) throws Exception {
		HashMap<Object,Object> result = new HashMap<>();
		if (param.isEmpty()) {
			//return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("파일이 없습니다.");
		}

		try {
			// 업로드 디렉토리가 없으면 생성
			File uploadDir = new File(UPLOAD_DIR);
			if (!uploadDir.exists()) {
				uploadDir.mkdirs();
			}

			// 파일 저장
			String filePath = UPLOAD_DIR + param.getOriginalFilename();
			param.transferTo(new File(filePath));

			//return ResponseEntity.ok("파일 업로드 성공: " + param.getOriginalFilename());
		} catch (IOException e) {
			//return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("파일 업로드 실패: " + e.getMessage());
		}
		return result;
	}

	public ResponseEntity<Resource> downloadFile(String fileName) throws Exception {
		try {
			Path filePath = Paths.get(UPLOAD_DIR).resolve(fileName).normalize();
			Resource resource = new UrlResource(filePath.toUri());

			if (!resource.exists()) {
				return ResponseEntity.notFound().build();
			}

			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
					.body(resource);
		} catch (MalformedURLException e) {
			return ResponseEntity.badRequest().build();
		}
	}

}
