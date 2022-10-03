package com.web.dev.solution;
/*
주식가격
https://school.programmers.co.kr/learn/courses/30/lessons/42584
 */

public class Solution42584 {

    public static void main(String[] args) {
        Solution42584 s1 = new Solution42584();
        s1.solution(new int[]{1,2,3,2,3}); //[4, 3, 1, 1, 0]
    }

    public int[] solution(int[] prices) {

        int length = prices.length;
        int[] answer = new int[length];

        int answerTemp;
        for(int i=0;i<length;i++){
            answerTemp = 1;
            for(int j=i+1;j<length;j++){
                if( prices[i] > prices[j]){
                    answer[i] = answerTemp;
                    break;
                }

                if( j == length-1){
                    answer[i] = answerTemp;
                    break;
                }

                answerTemp++;
            }
        }

        return answer;
    }
}
