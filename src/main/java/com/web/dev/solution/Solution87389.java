package com.web.dev.solution;

/*
나머지가 1이 되는 수 찾기
https://school.programmers.co.kr/learn/courses/30/lessons/87389
 */

public class Solution87389 {

    public static void main(String[] args) {

        Solution87389 s1 = new Solution87389();
        System.out.println(s1.solution(10));

        Solution87389 s2 = new Solution87389();
        System.out.println(s2.solution(12));

        Solution87389 s3 = new Solution87389();
        System.out.println(s3.solution(3));
    }

    public int solution(int n) {
        for(int i=2;i<n;i++){
            if( n % i == 1){
               return i;
            }
        }
        return 0;
    }
}