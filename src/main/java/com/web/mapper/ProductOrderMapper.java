package com.web.mapper;

import com.web.dto.wedding.ProductOrderDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductOrderMapper {

	int insertOrder(ProductOrderDTO dto);

	List<ProductOrderDTO> findByCustomerIdx(@Param("customerIdx") Long customerIdx);

	List<ProductOrderDTO> findAll();
}
