package com.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

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
}