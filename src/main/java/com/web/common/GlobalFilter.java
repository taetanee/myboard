package com.web.common;

import org.springframework.stereotype.Component;

import javax.servlet.*;

@Component
public class GlobalFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
        //TODO : 필터 전처리 작업필요할 시 넣자
    }
}
