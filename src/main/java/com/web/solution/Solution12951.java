package com.web.solution;

/*
JadeonCase 문자열 만들기
https://programmers.co.kr/learn/courses/30/lessons/12951?language=java
 */

public class Solution12951 {
    public static void main(String[] args) {
        Solution12951 s1 = new Solution12951();
        s1.solution("3people unFollowed me");
    }

    public String solution(String s) {
        String answer = "";

        boolean nextChar = true;
        for( char c : s.toCharArray()) {
            if(nextChar == true && c >= 'a' && c <= 'z'){
                answer += (char) (c - 32);
            } else if(nextChar == false && c >= 'A' && c <= 'Z'){
                answer += (char) (c + 32);
            } else {
                answer += c;
            }

            if(c == ' '){
                nextChar = true;
            } else {
                nextChar = false;
            }

        }
        return answer;
    }
}
