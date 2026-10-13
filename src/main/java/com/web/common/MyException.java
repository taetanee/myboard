package com.web.common;

import lombok.Getter;
import lombok.Setter;

public class MyException extends Exception {


	public static final int MD_ERR_EXCEPTION        =   1001;


	@Getter @Setter
	private int errCode;

	public MyException(int errCode) {
		this.errCode = errCode;
	}

	public MyException(Exception ex) {
		super(ex);
		if(ex instanceof MyException)
			this.errCode = ((MyException) ex).getErrCode();
		else
			this.errCode = MyException.MD_ERR_EXCEPTION;
	}

	public static String getMsg(int errCd) {
		switch (Integer.valueOf(errCd)) {
			case MD_ERR_EXCEPTION:
				return "Exception 발생";
			case Const.UNAUTHORIZED_ERROR:
				return Const.UNAUTHORIZED_ERROR_MSG;
			case Const.LOGIN_FAILED_ERROR:
				return Const.LOGIN_FAILED_ERROR_MSG;
			case Const.DUPLICATE_LOGIN_ID_ERROR:
				return Const.DUPLICATE_LOGIN_ID_ERROR_MSG;
			case Const.FORBIDDEN_ADMIN_ERROR:
				return Const.FORBIDDEN_ADMIN_ERROR_MSG;
			case Const.NOT_INVALID_PARAM_ERROR:
				return Const.NOT_INVALID_PARAM_ERROR_MSG;
		}
		return "알수없는 에러";
	}
}
