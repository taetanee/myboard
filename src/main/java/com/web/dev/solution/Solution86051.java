package com.web.dev.solution;

import java.util.ArrayList;
import java.util.Arrays;

/*
없는 숫자 더하기
https://programmers.co.kr/learn/courses/30/lessons/86051
 */
public class Solution86051 {

    public static void main(String args[]){

        Solution86051 s1 = new Solution86051();
        System.out.println(s1.solution(new int[] {1,2,3,4,6,7,8,0}));
        System.out.println(s1.solution(new int[] {5,8,4,0,6,7,9}));
    }

    public int solution(int[] numbers) {
        int answer = 0;
        ArrayList<Integer> standardAnswer = new ArrayList<Integer>(Arrays.asList(0,1,2,3,4,5,6,7,8,9));

        //[시작] 키에 해당하는 값 0으로 세팅
        for(int i=0;i<numbers.length;i++){
            standardAnswer.set(numbers[i],0);
        }
        //[종료] 키에 해당하는 값 0으로 세팅

        //[시작] 모두 더 함
        for(int i=0;i<standardAnswer.size();i++){
            answer += standardAnswer.get(i);
        }
        //[종료] 모두 더 함

        return answer;
    }
}
