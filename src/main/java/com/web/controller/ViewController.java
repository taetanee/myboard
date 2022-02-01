package com.web.controller;

import com.web.mapper.HouseMapper;
import com.web.service.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;

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

    @RequestMapping(value = "pre_equation")
    public Object page02(Model model, HttpServletRequest request) {
        String eUid = request.getParameter("e_uid");
        HashMap<String,Object> result = service.getPreEquation(eUid);
        if("".equals(result.get("LTV_MIN")) && "".equals(result.get("DTI")) && "".equals(result.get("DSR"))){
            return "stop";
        }
        return "pre_equation";
    }

    @RequestMapping(value = "calculator")
    public Object page03(Model model, HttpServletRequest request) {
        return "calculator";
    }
}