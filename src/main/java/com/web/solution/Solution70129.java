package com.web.solution;

/*
이진 변환 반복하기
https://school.programmers.co.kr/learn/courses/30/lessons/70129
 */

public class Solution70129 {
    public static void main(String[] args) {
        Solution70129 s1 = new Solution70129();
        System.out.println(s1.solution("110010101001"));

        Solution70129 s2 = new Solution70129();
        System.out.println(s2.solution("01110"));

        Solution70129 s3 = new Solution70129();
        System.out.println(s3.solution("1111111"));
    }


    public int[] solution(String s) {
        int[] answer = new int[2];
        String temp = s;

        int i = 0;
        int count = 0;
        while(!"1".equals(temp)){
            count += countZero(temp);
            temp = temp.replace("0","");
            temp = Integer.toBinaryString(temp.length());
            i++;
        }

        answer[0] = i;
        answer[1] = count;

        return answer;
    }

    public int countZero(String s){
        int result = 0;
        for(int i=0;i<s.length();i++){
            if( s.charAt(i) == '0'){
                result++;
            }
        }
        return result;
    }
}
