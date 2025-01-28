package com.web.common.controller;

import com.web.common.MyException;
import com.web.common.CommonResVO;
import com.web.common.Const;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
@Slf4j
public class ExceptionErrorController {
    @ExceptionHandler({	Exception.class })
    public ResponseEntity<?> exception(final Exception ex, ServletWebRequest servletRequest, HttpServletRequest request) {
        CommonResVO commonResVO = new CommonResVO();

        if(ex instanceof HttpRequestMethodNotSupportedException){
            log.warn("[warn] URI : " + request.getRequestURI() + " / exception e = " , ex);
            commonResVO.setResultCode(Const.BAD_REQUEST);
            commonResVO.setResultMsg(Const.BAD_REQUEST_MSG);
        } else if(ex instanceof MyException){
            log.warn("[warn] URI : " + request.getRequestURI() + " / exception e = " , ex);
            MyException myException = (MyException) ex;
            commonResVO.setResultCode(myException.getErrCode());
            commonResVO.setResultMsg(MyException.getMsg(myException.getErrCode()));
        } else {
            log.error("[error] URI : " + request.getRequestURI() + " / exception e = " , ex);
            commonResVO.setResultCode(Const.INTERNAL_SERVER_ERROR);
            commonResVO.setResultMsg(Const.INTERNAL_SERVER_ERROR_MSG);
        }

        return ResponseEntity.ok(commonResVO);
    }
}
