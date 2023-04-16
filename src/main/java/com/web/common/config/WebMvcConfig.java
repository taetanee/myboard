package com.web.common.config;


import com.web.common.Interceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

import java.util.Arrays;

@Configuration
@EnableWebMvc
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private Interceptor commonInterceptor;

    //로그인 체크 페턴
    private String[] includedUrlPatterns = {
            "/mypage/**"
    };

    //로그인 체크 예외
    private String[] excludedUrlPatterns = {
            //필요시 추가
    };


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(commonInterceptor)
                .addPathPatterns(Arrays.asList(includedUrlPatterns))
                .excludePathPatterns(Arrays.asList(excludedUrlPatterns))
        ;
    }

    private static final String[] CLASSPATH_RESOURCE_LOCATIONS = {
            "classpath:/resources/"
            , "classpath:/static/"
    };


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**").addResourceLocations(CLASSPATH_RESOURCE_LOCATIONS);
    }

}
