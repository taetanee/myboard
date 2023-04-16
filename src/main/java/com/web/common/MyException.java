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
		}
		return "알수없는 에러";
	}
}
