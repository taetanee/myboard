package com.web.solution;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/*
카펫
https://school.programmers.co.kr/learn/courses/30/lessons/42842?language=java
 */

public class Solution42842 {
    public static void main(String[] args) {
        Solution42842 s1 = new Solution42842();
        s1.solution(10, 2);
    }


    public int[] solution(int brown, int yellow) {

        int[] answer = {};

        Set<ArrayList> set = new HashSet<>();
        int temp = brown + yellow;
        for(int i=1;i<=temp;i++){
            if( temp % i == 0){
                ArrayList tempArrayList =new ArrayList();
                tempArrayList.add(i);
                tempArrayList.add(temp/i);
                set.add(tempArrayList);
            }
        }

        return answer;
    }
}