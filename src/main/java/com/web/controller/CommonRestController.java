package com.web.controller;

import com.web.common.CommonResVO;
import com.web.service.CommonService;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Api(tags="공통 컨트롤러")
@RestController
@RequestMapping("/")
@Slf4j
public class CommonRestController {

    @Autowired
    private CommonService commonService;


    @PostMapping("/getUuid")
    public ResponseEntity<?> getUuid(){
        CommonResVO response = new CommonResVO();
        HashMap<String,Object> result = commonService.getUuid();
        response.setResult(result);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/getTest")
    public Map getTest(HashMap<String,String> param){
        Map result = new HashMap<String, Object>();
        result.put("id", "taetanee");
        result.put("name", "테타니");
        return result;
    }


    @PostMapping("/exceptionTest")
    public void exceptionTest() throws Exception {
        throw new Exception("exceptionTest");
    }


    @PostMapping("/checkHealth")
    public ResponseEntity<?> checkHealth(HashMap<String,String> param){
        return ResponseEntity.ok(commonService.checkHealth());
    }

}
