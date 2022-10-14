package com.web.dev.solution;

/*
옹알이
https://school.programmers.co.kr/learn/courses/30/lessons/120956?language=java
 */

public class Solution120956 {
    public static void main(String[] args) {

        Solution120956 s1 = new Solution120956();
        System.out.println(s1.solution(new String[]{"aya", "yee", "u", "maa"}));// 1("aya")

        Solution120956 s2 = new Solution120956();
        System.out.println(s2.solution(new String[]{"ayaye", "uuu", "yeye", "yemawoo", "ayaayaa"}));//2("ayaye","yemawoo")
    }
    public int solution(String[] babbling) {
        int answer = 0;

        String[] nonSpeaking = new String[]{"ayaaya","yeye","woowoo","mama"};
        String[] speaking = new String[]{"aya", "ye", "woo", "ma"};

        for(int i=0;i<babbling.length;i++){
            String str = babbling[i];
            for(int j=0;j<nonSpeaking.length;j++){
                str = str.replace(nonSpeaking[j],"XXX");
            }

            for(int j=0;j<speaking.length;j++){
                str = str.replace(speaking[j],"");
            }

            if(str.length() == 0){
                answer++;
            }
        }

        return answer;
    }
}
