package com.web.dev;

/*
피보나치 수
https://programmers.co.kr/learn/courses/30/lessons/12945
 */

public class Solution12945 {
    public static void main(String[] args) {

        //Solution12945 s1 = new Solution12945();
        //System.out.println(s1.solution(3));
        //Solution12945 s2 = new Solution12945();
        //System.out.println(s2.solution(5));
        Solution12945 s3 = new Solution12945();
        System.out.println(s3.solution(100));
    }

    public int solution(int n) {
        int answer = 0;

        int a0 = 0;
        int a1 = 1;

        for (int i = 1; i < n; i++) {
            answer = (a0 + a1) % 1234567;
            a0 = a1 % 1234567;
            a1 = answer % 1234567;
        }

        return answer;
    }

//    public int solution(int n) {
//        int answer = 0;
//
//        if( n == 0 ){
//            return 0;
//        }
//
//        if( n == 1 ){
//            return 1;
//        }
//        answer = solution(n - 2) + solution(n - 1);
//        return answer % 1234567;
//    }
}
