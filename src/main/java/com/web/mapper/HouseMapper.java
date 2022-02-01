package com.web.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;

@Mapper
public interface HouseMapper {
    List<HashMap<String,Object>> getQuestion(@Param("qUid") String qUid);

    HashMap<String,Object> getPreEquation(@Param("eUid") String eUid);
}
