package com.web.mapper;

import com.web.dto.wedding.WeddingCustomerDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface WeddingCustomerMapper {

	WeddingCustomerDTO findByLoginId(@Param("loginId") String loginId);

	WeddingCustomerDTO findBySlug(@Param("slug") String slug);

	WeddingCustomerDTO findByIdx(@Param("idx") Long idx);

	int existsLoginId(@Param("loginId") String loginId);

	int existsSlug(@Param("slug") String slug);

	int insertCustomer(WeddingCustomerDTO dto);

	int updateInviteJson(@Param("idx") Long idx, @Param("inviteJson") String inviteJson);

	int updateVideoJson(@Param("idx") Long idx, @Param("videoJson") String videoJson);
}
