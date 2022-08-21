package com.web.solution;

/*
약수의 개수와 덧셈
https://school.programmers.co.kr/learn/courses/30/lessons/77884
 */

import java.util.HashMap;

public class Solution77884 {
    public static void main(String[] args) {

        Solution77884 s1 = new Solution77884();
        System.out.println(s1.solution(13,17));

        Solution77884 s2 = new Solution77884();
        System.out.println(s2.solution(24, 27));

    }
    public int solution(int left, int right) {
        int answer = 0;
        HashMap<Integer, Integer> hashMap = new HashMap<>();
        for(int i=left;i<=right;i++){
            hashMap.put(i,yak(i));
        }

        for(int key : hashMap.keySet()){
            if( hashMap.get(key) % 2 == 0){
                answer += key;
            } else {
                answer -= key;
            }
        }

        return answer;
    }

    public int yak(int num) {
        int cnt = 0;
        for (int a = 1; a <= num; a++) {
            if ((num % a) == 0) {
                cnt++;
            }
        }
        return cnt;
    }

}
