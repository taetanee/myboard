package com.web.dev.solution;


/*
로또의 최고 순위와 최저 순위
https://programmers.co.kr/learn/courses/30/lessons/77484
 */

public class Solution77484 {

    public static void main(String[] args) {
        Solution77484 s1 = new Solution77484();
        System.out.println(s1.solution(new int[]{44, 1, 0, 0, 31, 25},new int[]{31, 10, 45, 1, 6, 19}));

        Solution77484 s2 = new Solution77484();
        System.out.println(s2.solution(new int[]{0, 0, 0, 0, 0, 0},new int[]{38, 19, 20, 40, 15, 25}));

        Solution77484 s3 = new Solution77484();
        System.out.println(s3.solution(new int[]{45, 4, 35, 20, 3, 9},new int[]{20, 9, 3, 45, 4, 35}));
    }

    public int[] solution(int[] lottos, int[] win_nums) {
        //int[] answer = {};
        int[] answer = new int[2];

        int matchingCnt = 0;
        int anonymousCnt = 0;
        for(int i=0;i<lottos.length;i++){

            for(int j=0;j<win_nums.length;j++) {
                if( lottos[i] == win_nums[j]){
                    matchingCnt++;
                }
            }

            if( lottos[i] == 0){
                anonymousCnt++;
            }
        }

        answer[0] = getRanking(matchingCnt + anonymousCnt);
        answer[1] = getRanking( matchingCnt );

        return answer;
    }

    public int getRanking( int matchingCnt ){

        if( matchingCnt == 6){
            return 1;
        } else if( matchingCnt == 5){
            return 2;
        } else if( matchingCnt == 4){
            return 3;
        } else if( matchingCnt == 3){
            return 4;
        } else if( matchingCnt == 2){
            return 5;
        } else {
            return 6;
        }
    }
}
