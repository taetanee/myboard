package com.web.common;

import lombok.Getter;
import lombok.Setter;

public class Except extends Exception {


	public static final int MD_ERR_EXCEPTION        =   1001;


	@Getter @Setter
	private int errCode;

	public Except(int errCode) {
		this.errCode = errCode;
	}

	public Except(Exception ex) {
		super(ex);
		if(ex instanceof Except)
			this.errCode = ((Except) ex).getErrCode();
		else
			this.errCode = Except.MD_ERR_EXCEPTION;
	}

	public static String getMsg(int errCd) {
		switch (Integer.valueOf(errCd)) {
			case MD_ERR_EXCEPTION:
				return "Exception 발생";
		}
		return "알수없는 에러";
	}
}
