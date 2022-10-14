package com.web.common;

import lombok.Getter;
import lombok.Setter;

public class CommonException extends Exception {

	@Getter
	@Setter
	private int errCode;

	public CommonException(int errCode) {
		this.errCode = errCode;
	}
}
