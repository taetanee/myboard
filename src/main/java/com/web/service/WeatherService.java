package com.web.service;


import com.web.common.CommonException;
import com.web.common.DataGoAPI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.rmi.server.ExportException;
import java.util.HashMap;

@Slf4j
@Service
public class WeatherService {

	@Autowired
	private DataGoAPI dataGoAPI;


	public HashMap<Object,Object> getShortTermWeather(HashMap<String,String> param) throws Exception{

		if( false ){
			throw new CommonException(CommonException.MD_ERR_EXCEPTION);
		}

		HashMap<Object,Object> result = new HashMap<>();
		result = dataGoAPI.getShortTermWeather(param);
		return result;
	}
}
