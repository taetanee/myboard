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
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

	private static final String FILED_NAME_RANDOM_WORD = "randomWord";

	private static final String FILED_NAME_KEY_CONTENT = "content";

	public String getRandomWord() throws Exception {
		String result = new String();
		result = commonUtil.getUUID(3);
		return result;
	}

	public String saveContent(HashMap<String,Object> param) throws Exception {
		String result = new String();
		String keyContent = FILED_NAME_KEY_CONTENT;
		String dataContent = (String) param.get(keyContent);

		String keyUrl = FILED_NAME_RANDOM_WORD;
		String urlData = (String) param.get(keyUrl);

		if( urlData == null ){
			urlData = "";
		}

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

		String keyUrl = FILED_NAME_RANDOM_WORD;
		String urlData = (String) param.get(keyUrl);

		if( urlData == null ){
			urlData = "";
		}

		resultx = redisUtil.getValues(urlData);
		result.put("data",resultx);
		return result;
	}


	public HashMap<Object,Object> uploadFile(MultipartFile param, String randomWord) throws Exception {
		HashMap<Object,Object> result = new HashMap<>();
		if (param.isEmpty()) {
			throw new MyException(Const.NOT_INVALID_PARAM_ERROR);
		}

		// randomWord별 디렉토리에 저장
		String dirPath = UPLOAD_DIR + randomWord + "/";
		File uploadDir = new File(dirPath);
		if (!uploadDir.exists()) {
			uploadDir.mkdirs();
		}

		String filePath = dirPath + param.getOriginalFilename();
		param.transferTo(new File(filePath));

		result.put("fileName", param.getOriginalFilename());
		return result;
	}

	public ResponseEntity<Resource> downloadFile(String randomWord, String fileName) throws Exception {
		try {
			Path filePath = Paths.get(UPLOAD_DIR + randomWord + "/").resolve(fileName).normalize();
			Resource resource = new UrlResource(filePath.toUri());

			if (!resource.exists()) {
				return ResponseEntity.notFound().build();
			}

			String encodedFileName = URLEncoder.encode(resource.getFilename(), "UTF-8").replace("+", "%20");
			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName)
					.body(resource);
		} catch (MalformedURLException e) {
			return ResponseEntity.badRequest().build();
		}
	}

	public List<String> getFileList(String randomWord) throws Exception {
		List<String> fileNames = new ArrayList<>();
		String dirPath = UPLOAD_DIR + randomWord + "/";
		File dir = new File(dirPath);

		if (dir.exists() && dir.isDirectory()) {
			File[] files = dir.listFiles();
			if (files != null) {
				for (File file : files) {
					if (file.isFile()) {
						fileNames.add(file.getName());
					}
				}
			}
		}
		return fileNames;
	}

}
