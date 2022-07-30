package com.web.solution;

/*
모의고사
https://programmers.co.kr/learn/courses/30/lessons/42840
 */

import java.util.ArrayList;

public class Solution42840 {
    public static void main(String[] args) {

        Solution42840 s1 = new Solution42840();
        System.out.println(s1.solution(new int[]{1,2,3,4,5}));
        Solution42840 s2 = new Solution42840();
        System.out.println(s2.solution(new int[] {1,3,2,4,2}));
    }
    public int[] solution(int[] answers) {

        int[] student1 = {1,2,3,4,5};
        int[] student2 = {2,1,2,3,2,4,2,5};
        int[] student3 = {3,3,1,1,2,2,4,4,5,5};

        int[] score = {0,0,0};

        for(int i=0;i<answers.length;i++){
            if( student1[i%student1.length] == answers[i] ){
                score[0]++;
            }
            if( student2[i%student2.length] == answers[i] ){
                score[1]++;
            }
            if( student3[i%student3.length] == answers[i] ){
                score[2]++;
            }
        }

        int max = 0;
        for(int i=0;i<score.length;i++){
            if( max < score[i] ) {
                max = score[i];
            }
        }

        ArrayList<Integer> tempAnswer = new ArrayList();
        for(int i=0;i<score.length;i++){
            if( max == score[i] ) {
                tempAnswer.add(i+1);
            }
        }

        int[] answer = new int[tempAnswer.size()];
        for(int i=0;i<tempAnswer.size();i++){
            answer[i] = tempAnswer.get(i);
        }
        return answer;
    }
}
