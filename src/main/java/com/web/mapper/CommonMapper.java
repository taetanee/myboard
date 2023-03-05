package com.web.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.HashMap;

@Mapper
public interface CommonMapper {
	HashMap<String,String> checkHealth();
}
