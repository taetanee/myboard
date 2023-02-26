package com.web.controller;

import com.web.common.CommonException;
import com.web.common.CommonResVO;
import com.web.common.CommonError;
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
            commonResVO.setResultCode(CommonError.BAD_REQUEST);
            commonResVO.setResultMsg(CommonError.BAD_REQUEST_MSG);
        } else if(ex instanceof CommonException){
            log.warn("[warn] URI : " + request.getRequestURI() + " / exception e = " , ex);
            commonResVO.setResultCode(CommonError.BAD_REQUEST);
            commonResVO.setResultMsg(CommonError.BAD_REQUEST_MSG);
        } else {
            log.error("[error] URI : " + request.getRequestURI() + " / exception e = " , ex);
            commonResVO.setResultCode(CommonError.INTERNAL_SERVER_ERROR);
            commonResVO.setResultMsg(CommonError.INTERNAL_SERVER_ERROR_MSG);
        }
        return ResponseEntity.ok(commonResVO);
    }
}
