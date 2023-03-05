package com.web.controller;

import com.web.service.CommonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class ViewController {

    @Autowired
    private CommonService service;

    @RequestMapping(value = {"","index"})
    public Object index(Model model, HttpServletRequest request) {
        return "index";
    }

    @RequestMapping("dev/devThis")
    public Object devThis(Model model, HttpServletRequest request) {
        return "dev/devThis";
    }

    @RequestMapping("dev/exceptionTest")
    public Object exceptionTest(Model model, HttpServletRequest request) {
        return "dev/exceptionTest";
    }
}