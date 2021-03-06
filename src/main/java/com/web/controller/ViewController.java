package com.web.controller;

import com.web.service.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

@Controller
public class ViewController {

    @Autowired
    private ServiceImpl service;

    @RequestMapping("dev")
    public Object dev()
    {
        return "dev";
    }

    @RequestMapping(value = {"","index"})
    public Object page01(Model model, HttpServletRequest request) {
        return "index";
    }
}