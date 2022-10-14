package com.web.dev.solution;


/*
[스킬 체크 테스트 Level 1] 10진수 -> 3진수 -> 반전 -> 10진수
https://programmers.co.kr/skill_checks/377501?challenge_id=6771
 */

public class Solution377501 {

    public static void main(String[] args) {
        Solution377501 s1 = new Solution377501();
        System.out.println(s1.solution(45));
        Solution377501 s2 = new Solution377501();
        System.out.println(s1.solution(125));

    }


    public int solution(int n) {
        int answer = 0;
        String three = Integer.toString(n,3);
        StringBuffer sb = new StringBuffer(three);
        String reverse = sb.reverse().toString();
        answer = Integer.parseInt(reverse,3);
        return answer;
    }
}


