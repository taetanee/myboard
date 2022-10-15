package com.web.controller;

import com.web.common.CommonResVO;
import com.web.common.CommonError;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionErrorController {
    @ExceptionHandler({	Exception.class })
    public ResponseEntity<?> exception(final Exception ex) {
        CommonResVO commonResVO = new CommonResVO();
        if(ex instanceof HttpRequestMethodNotSupportedException){
            commonResVO.setResultCode(CommonError.BAD_REQUEST);
            commonResVO.setResultMsg(CommonError.BAD_REQUEST_MSG);
        } else {
            commonResVO.setResultCode(CommonError.INTERNAL_SERVER_ERROR);
            commonResVO.setResultMsg(CommonError.INTERNAL_SERVER_ERROR_MSG);
        }
        return ResponseEntity.ok(commonResVO);
    }
}
