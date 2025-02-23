package com.web.controller;

import com.web.service.CommonService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;



@Controller
public class ViewController {

    @GetMapping(value = {"","/index"})
    public Object index(Model model, HttpServletRequest request) {
        return "/index";
    }

    @GetMapping("/dev/devThis")
    public Object devThis(Model model, HttpServletRequest request) {
        return "/dev/devThis";
    }

    @GetMapping("/dev/exceptionTest")
    public Object exceptionTest(Model model, HttpServletRequest request) {
        return "/dev/exceptionTest";
    }

    @GetMapping("/clipboard")
    public Object clipboard(Model model, HttpServletRequest request) {
        return "/clipboard";
    }
}