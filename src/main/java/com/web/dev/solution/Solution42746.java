package com.web.dev.solution;

import java.util.Arrays;

//https://gofo-coding.tistory.com/entry/%ED%94%84%EB%A1%9C%EA%B7%B8%EB%9E%98%EB%A8%B8%EC%8A%A4-42746%EB%B2%88-%EA%B0%80%EC%9E%A5-%ED%81%B0-%EC%88%98
//참고해서 풀었음

/*
가장 큰 수
https://programmers.co.kr/learn/courses/30/lessons/42746?language=java
 */

public class Solution42746 {

    public static void main(String args[]){

        Solution42746 s1 = new Solution42746();
        System.out.println(s1.solution(new int[] {6, 10, 2})); //6210
        System.out.println(s1.solution(new int[] {3, 30, 34, 5, 9})); //9534330
    }

    public String solution(int[] numbers) {
        String answer = "";
        String[] answerTemp = new String[numbers.length];

        for(int i=0;i<numbers.length;i++){
            answerTemp[i] = String.valueOf(numbers[i]);
        }

        Arrays.sort(answerTemp,(o1,o2)-> -(o1+o2).compareTo(o2+o1));

        for(int i=0;i<answerTemp.length;i++){
            answer += answerTemp[i];
        }

        answer = answer.replaceAll("^0+", "");
        if(answer.length() == 0) answer = "0";

        return answer;
    }


}
