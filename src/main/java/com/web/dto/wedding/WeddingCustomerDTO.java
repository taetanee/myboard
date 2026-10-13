package com.web.dto.wedding;

import lombok.Data;

@Data
public class WeddingCustomerDTO {

	private Long idx;
	private String loginId;
	private String password;
	private String slug;
	private String customerName;
	private String phone;
	private String naverOrderNo;
	private String status;
	private String inviteJson;
	private String videoJson;
	private String cDate;
	private String uDate;
}
