package com.web.common;

import lombok.Getter;
import lombok.Setter;

public class CommonException extends Exception {


	public static final int MD_ERR_EXCEPTION        =   1001;


	@Getter @Setter
	private int errCode;

	public CommonException(int errCode) {
		this.errCode = errCode;
	}

	public CommonException(Exception ex) {
		super(ex);
		if(ex instanceof CommonException)
			this.errCode = ((CommonException) ex).getErrCode();
		else
			this.errCode = CommonException.MD_ERR_EXCEPTION;
	}

	public static String getMsg(int errCd) {
		switch (Integer.valueOf(errCd)) {
			case MD_ERR_EXCEPTION:
				return "Exception 발생";
		}
		return "알수없는 에러";
	}
}
