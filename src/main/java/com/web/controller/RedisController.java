package com.web.controller;

import com.web.service.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.sql.Timestamp;
import java.util.Set;

@Controller
public class RedisController {
//레디스 테스트

	@Autowired
	RedisUtil redisUtil;

	@RequestMapping(value = "/redis/test/setString")
	@ResponseBody
	public void setValue(String testkey, String testvalue){
		Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
		redisUtil.setValues(testkey,testvalue);
	}

	@RequestMapping(value = "/redis/test/getString")
	@ResponseBody
	public String getValue(String testkey){
		return redisUtil.getValues(testkey);
	}


	@RequestMapping(value = "/redis/test/setSets")
	@ResponseBody
	public void setSets(String testkey,String... testvalues){
		redisUtil.setSets(testkey,testvalues);
	}

	@RequestMapping(value = "/redis/test/getSets")
	@ResponseBody
	public Set getSets(String key){
		return redisUtil.getSets(key);
	}

}