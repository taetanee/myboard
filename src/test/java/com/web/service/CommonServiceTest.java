package com.web.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;


import static org.hamcrest.core.Is.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(
        properties = {
                "testId=taetanee"
                , "testName=테타니"
        }
)
@Transactional
@AutoConfigureMockMvc
@Slf4j
class CommonServiceTest {

    @Value("${testId}")
    private String testId;

    @Value("${testName}")
    private String testName;

    @Autowired
    MockMvc mvc;

    //@Autowired
    //private TestRestTemplate restTemplate;

    @Autowired
    private CommonService commonService;

    @Autowired
    private WebApplicationContext ctx;

    @BeforeEach() //Junit4의 @Before
    public void setup() {
        this.mvc = MockMvcBuilders.webAppContextSetup(ctx)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))  // 필터 추가
                .alwaysDo(print())
                .build();
    }

    @Test
    void getCovid() throws Exception {
        System.out.println("##### Properties 테스트 #####");
        System.out.println("testId : " + testId);
        System.out.println("testName : " + testName);

        /******** START : MOC MVC test **********/
        System.out.println("******** START : MOC MVC test **********");
        mvc.perform(get("/getTest"))
                .andExpect(status().isOk())
                //.andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.id", is(testId)))
                .andExpect(jsonPath("$.name", is(testName)))
                .andDo(print());
        System.out.println("******** END : MOC MVC test **********");
        /******** END : MOC MVC test **********/

    }
}