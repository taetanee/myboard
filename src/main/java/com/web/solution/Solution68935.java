package com.web.solution;

/*
3진법 뒤집기
https://school.programmers.co.kr/learn/courses/30/lessons/68935
 */

public class Solution68935 {
    public static void main(String[] args) {
        Solution68935 s1 = new Solution68935();
        System.out.println(s1.solution(45));

        Solution68935 s2 = new Solution68935();
        System.out.println(s2.solution(125));
    }

    public int solution(int n) {
        int answer = 0;

        //10진법->3진법
        String three = Integer.toString(n,3);

        //3진법 반전
        String[] tempArray  = three.split("");
        String reverse = "";
        for(int i=tempArray.length-1;i>=0;i--) {
            reverse += tempArray[i];
        }

        //3진법->10진법
        answer = Integer.parseInt(reverse,3);
        return answer;
    }
}
