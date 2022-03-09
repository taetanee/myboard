package com.web.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "covid")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Covid {
    private double accDefRate;
    private int accExamCnt;
    private String stateTime;
    private int deathCnt;
    private int decideCnt;
    private int stateDt;
    private String updateDt;
    private String createDt;
    private int seq;
}
