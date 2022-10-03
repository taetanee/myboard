package com.web.dev.solution;

/*
최소직사각형
https://school.programmers.co.kr/learn/courses/30/lessons/86491?language=java
https://jongwoon.tistory.com/79 : 참고해서 풀었음
 */

public class Solution86491 {

    public static void main(String[] args) {
        Solution86491 s1 = new Solution86491();
        s1.solution(new int[][] {{60, 50}, {30, 70}, {60, 30}, {80, 40}});
    }
    public int solution(int[][] sizes) {
        int maxWidth = -1, maxHeight = -1;
        for(int[] size : sizes) {
            maxWidth = Math.max(Math.min(size[0], size[1]), maxWidth);
            maxHeight = Math.max(Math.max(size[0], size[1]), maxHeight);
        }
        return maxWidth * maxHeight;
    }
}
