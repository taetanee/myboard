package com.web.common;

public class Const {

	public static final String NX = "62";
	public static final String NY = "125";

	public static final int BAD_REQUEST = 400;
	public static final String BAD_REQUEST_MSG = "Bad Request";

	public static final int INTERNAL_SERVER_ERROR = 500;
	public static final String INTERNAL_SERVER_ERROR_MSG = "Internal Server Error";

	public static final int NO_DATA_API_ERROR = 1001;
	public static final String NO_DATA_API_ERROR_MSG = "body가 널이거나 빈값임";

	public static final int NOT_INVALID_PARAM_ERROR = 1002;
	public static final String NOT_INVALID_PARAM_ERROR_MSG = "파리미터가 올바르지 않음";

	public static final int UNAUTHORIZED_ERROR = 1003;
	public static final String UNAUTHORIZED_ERROR_MSG = "로그인이 필요합니다";

	public static final int LOGIN_FAILED_ERROR = 1004;
	public static final String LOGIN_FAILED_ERROR_MSG = "아이디 또는 비밀번호가 올바르지 않습니다";

	public static final int DUPLICATE_LOGIN_ID_ERROR = 1005;
	public static final String DUPLICATE_LOGIN_ID_ERROR_MSG = "이미 사용중인 아이디입니다";

	public static final int FORBIDDEN_ADMIN_ERROR = 1006;
	public static final String FORBIDDEN_ADMIN_ERROR_MSG = "관리자 인증이 올바르지 않습니다";

}