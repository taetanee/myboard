package com.web.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;

@Mapper
public interface TestMapper {
	HashMap<String,String> checkHealth();
}
