package com.web.service;

import com.web.vo.CovidVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import org.junit.Before;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.closeTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;


@ExtendWith(MockitoExtension.class)
public class ServiceImplUnitTest {

    @InjectMocks
    private ServiceImpl serviceImpl;

    @Before
    public void setup(){
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void test1() {
        try {
            //given
            double x1 = 3.3;

            //when
            double x2 = 3.5;
            CovidVO y2 = serviceImpl.unitTest();

            //then
            assertThat(x1, closeTo(x2,0.3));

            assertThat(y2.getUpdateDt(), is("A"));
        } catch (Exception e){
            System.out.println(e);
        }
    }

    //@Test
    public void test2() {
        try {
            //given
            double x1 = 3.3;

            //when
            double x2 = 3.5;
            CovidVO y2 = serviceImpl.unitTest();

            //then
            assertThat(x1, closeTo(x2,0.1)); //이 경우에는 error 발생
        } catch (Exception e){
            System.out.println(e);
        }
    }

    //@Test
    public void test3() {
        try {
            //given
            double x1 = 3.3;

            //when
            double x2 = 3.5;
            CovidVO y2 = serviceImpl.unitTest();

            //then
            assertThat(y2.getUpdateDt(), is("B")); //이 경우에는 error 발생
        } catch (Exception e){
            System.out.println(e);
        }
    }
}
