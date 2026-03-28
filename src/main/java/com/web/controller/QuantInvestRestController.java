package com.web.controller;

import com.web.service.QuantInvestService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Api(tags = "퀀트 투자 컨트롤러")
@RestController
@RequestMapping("/quantInvest")
@Slf4j
public class QuantInvestRestController {

    @Autowired
    private QuantInvestService quantInvestService;

    /**
     * 스크리닝 결과 목록 (페이징)
     * page: 0-based, size: 한 페이지 당 개수, sector: 섹터 필터, search: 종목코드/기업명 검색
     */
    @GetMapping("/getList")
    public ResponseEntity<?> getList(
            @RequestParam(defaultValue = "0")   int page,
            @RequestParam(defaultValue = "20")  int size,
            @RequestParam(defaultValue = "ALL") String sector,
            @RequestParam(defaultValue = "")    String search) {
        try {
            return ResponseEntity.ok(quantInvestService.getScreeningList(page, size, sector, search));
        } catch (Exception e) {
            return error(e);
        }
    }

    /** 퀄리티 전략 목록 (High ROE + High GP/A) */
    @GetMapping("/getQualityList")
    public ResponseEntity<?> getQualityList(
            @RequestParam(defaultValue = "0")   int page,
            @RequestParam(defaultValue = "20")  int size,
            @RequestParam(defaultValue = "ALL") String sector,
            @RequestParam(defaultValue = "")    String search) {
        try {
            return ResponseEntity.ok(quantInvestService.getQualityList(page, size, sector, search));
        } catch (Exception e) {
            return error(e);
        }
    }

    /** 소형주 전략 목록 (Low Market Cap + High Revenue Growth) */
    @GetMapping("/getSmallCapList")
    public ResponseEntity<?> getSmallCapList(
            @RequestParam(defaultValue = "0")   int page,
            @RequestParam(defaultValue = "20")  int size,
            @RequestParam(defaultValue = "ALL") String sector,
            @RequestParam(defaultValue = "")    String search) {
        try {
            return ResponseEntity.ok(quantInvestService.getSmallCapList(page, size, sector, search));
        } catch (Exception e) {
            return error(e);
        }
    }

    /** 모멘텀 전략 목록 (High 52-week Return + High Revenue Growth) */
    @GetMapping("/getMomentumList")
    public ResponseEntity<?> getMomentumList(
            @RequestParam(defaultValue = "0")   int page,
            @RequestParam(defaultValue = "20")  int size,
            @RequestParam(defaultValue = "ALL") String sector,
            @RequestParam(defaultValue = "")    String search) {
        try {
            return ResponseEntity.ok(quantInvestService.getMomentumList(page, size, sector, search));
        } catch (Exception e) {
            return error(e);
        }
    }

    /** 총합 랭킹 목록 (네 전략 점수 평균) */
    @GetMapping("/getCombinedList")
    public ResponseEntity<?> getCombinedList(
            @RequestParam(defaultValue = "0")   int page,
            @RequestParam(defaultValue = "20")  int size,
            @RequestParam(defaultValue = "ALL") String sector,
            @RequestParam(defaultValue = "")    String search) {
        try {
            return ResponseEntity.ok(quantInvestService.getCombinedRankingList(page, size, sector, search));
        } catch (Exception e) {
            return error(e);
        }
    }

    /** 섹터 목록 */
    @GetMapping("/getSectors")
    public ResponseEntity<?> getSectors(@RequestParam(defaultValue = "value") String strategy) {
        try {
            return ResponseEntity.ok(quantInvestService.getSectors(strategy));
        } catch (Exception e) {
            return error(e);
        }
    }

    /** 캐시 상태 (마지막 갱신 시각, 총 종목 수) */
    @GetMapping("/getStatus")
    public ResponseEntity<?> getStatus() {
        return ResponseEntity.ok(quantInvestService.getStatus());
    }

    /** 데이터 수집 즉시 실행 (수동 갱신) */
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh() {
        new Thread(() -> {
            try {
                quantInvestService.refreshData();
            } catch (Exception e) {
                log.error("[QuantInvest] 수동 갱신 실패: {}", e.getMessage());
            }
        }, "quant-invest-refresh").start();

        Map<String, Object> res = new HashMap<>();
        res.put("result", "started");
        res.put("message", "S&P 500 데이터 수집을 시작했습니다. 약 30~60초 후 완료됩니다.");
        return ResponseEntity.ok(res);
    }

    private ResponseEntity<Map<String, Object>> error(Exception e) {
        log.error("QuantInvest error: {}", e.getMessage());
        Map<String, Object> err = new HashMap<>();
        err.put("error", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
    }
}
