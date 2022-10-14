package com.web.dev.solution;

/*
[스킬 체크 테스트 Level 1] 내적 구하기 ( 그냥 1차원 배열의 곱의 합임 )
https://programmers.co.kr/skill_checks/377501?challenge_id=6771
 */

public class Solution377501_2 {

    public static void main(String[] args) {
        Solution377501_2 s1 = new Solution377501_2();
        System.out.println(s1.solution(new int[]{1,2,3,4},new int[]{-3,-1,0,2}));
        Solution377501_2 s2 = new Solution377501_2();
        System.out.println(s1.solution(new int[]{-1,0,1},new int[]{1,0,-1}));
    }


    public int solution(int[] a, int[] b) {
        int answer = 0;
        int length = a.length;

        for(int i=0;i<length;i++){
            answer += a[i] * b[i];
        }
        return answer;
    }
}
