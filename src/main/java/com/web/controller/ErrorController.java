package com.web.controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

@Controller
public class ErrorController implements org.springframework.boot.web.servlet.error.ErrorController {
	@RequestMapping("/errorPath")
	public ModelAndView handleError(HttpServletRequest request) {
		ModelAndView mav = new ModelAndView("error");

		int errorCode = (int) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
		if(errorCode == HttpStatus.BAD_REQUEST.value()){
			mav.addObject("errorMsg", HttpStatus.BAD_REQUEST.getReasonPhrase());
			mav.addObject("errorCode", HttpStatus.BAD_REQUEST.value());
		} else if(errorCode == HttpStatus.UNAUTHORIZED.value()){
			mav.addObject("errorMsg", HttpStatus.UNAUTHORIZED.getReasonPhrase());
			mav.addObject("errorCode", HttpStatus.UNAUTHORIZED.value());
		} else if(errorCode == HttpStatus.FORBIDDEN.value()){
			mav.addObject("errorMsg", HttpStatus.FORBIDDEN.getReasonPhrase());
			mav.addObject("errorCode", HttpStatus.FORBIDDEN.value());
		} else if(errorCode == HttpStatus.NOT_FOUND.value()){
			mav.addObject("errorMsg", HttpStatus.NOT_FOUND.getReasonPhrase());
			mav.addObject("errorCode", HttpStatus.NOT_FOUND.value());
		} else {
			mav.addObject("errorMsg", "Unknown Error");
			mav.addObject("errorCode", "?");
		}

		return mav;
	}
}
