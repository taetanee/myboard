package com.web.dto.wedding;

import lombok.Data;

@Data
public class ProductOrderDTO {

	private Long idx;
	private Long customerIdx;
	private String productType;
	private String sampleCode;
	private Integer quantity;
	private String recipientName;
	private String recipientPhone;
	private String shippingAddr;
	private String memo;
	private String status;
	private String cDate;
	private String uDate;
}
