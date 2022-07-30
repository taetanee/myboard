package com.web.solution;


/*
타겟넘버
https://school.programmers.co.kr/learn/courses/30/lessons/43165
(https://hyojun.tistory.com/entry/Programmers-%ED%83%80%EA%B2%9F-%EB%84%98%EB%B2%84-Java?category=980310)
 */

public class Solution43165 {

    public static int answer = 0;

    public static void main(String[] args) {
        Solution43165 s1 = new Solution43165();
        s1.solution(new int[]{1, 1, 1, 1, 1}, 3);
        //s1.solution(new int[]{4, 1, 2, 1}, 4);
        System.out.println(s1.answer);
    }

    public int solution(int[] numbers, int target) {
        dfs(numbers, target, 0, 0);
        return answer;
    }

    public void dfs(int[] number, int target, int sum, int dept){
        if( number.length == dept ){
            if( target == sum){
                answer++;
                return;
            }
        } else {
            dfs(number, target, sum + number[dept], dept + 1);
            dfs(number, target, sum - number[dept], dept + 1);
        }
    }
}