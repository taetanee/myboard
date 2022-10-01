package com.web.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;

@Setter @Getter
public class CommonResVO {

    @JsonProperty("result_code")
    private int resultCode;

    @JsonProperty("result_msg")
    private String resultMsg;

    @JsonProperty("result")
    private Object result;
}
