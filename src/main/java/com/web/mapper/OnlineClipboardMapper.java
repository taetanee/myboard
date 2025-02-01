package com.web.mapper;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;

@Mapper
public interface OnlineClipboardMapper {
	HashMap<String,String> getRandomWord(@Param("category") String qUid);
}
