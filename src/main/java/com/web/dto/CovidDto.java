package com.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "covid")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CovidDto {
    private double accDefRate;
    private int accExamCnt;
    private String stateTime;
    private int deathCnt;
    private int decideCnt;
    private int stateDt;
    private String updateDt;
    private String createDt;

    @Id
    private int seq;
}
