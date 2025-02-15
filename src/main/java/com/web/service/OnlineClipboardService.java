package com.web.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.web.common.util.CommonUtil;
import com.web.mapper.OnlineClipboardMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Slf4j
@Service
public class OnlineClipboardService {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private CommonUtil commonUtil;

	@Autowired
	private OnlineClipboardMapper onlineClipboardMapper;

//	public ArrayList getRandomWord() throws Exception {
//		ArrayList result = new ArrayList();
//		result.add(onlineClipboardMapper.getRandomWord("1"));
//		result.add(onlineClipboardMapper.getRandomWord("2"));
//		return result;
//	}

	public String getRandomWord() throws Exception {
		String result = new String();
		result = commonUtil.getUUID(3);
		return result;
	}
}
