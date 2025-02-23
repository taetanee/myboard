package com.web.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.web.common.CommonResponse;
import com.web.common.Const;
import com.web.common.MyException;
import com.web.common.util.CommonUtil;
import com.web.common.util.RedisUtil;
import com.web.mapper.OnlineClipboardMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

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
}
