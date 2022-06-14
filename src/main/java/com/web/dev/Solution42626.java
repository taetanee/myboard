package com.web.dev;

import java.util.PriorityQueue;

//https://darmk.tistory.com/entry/Programmers-%ED%94%84%EB%A1%9C%EA%B7%B8%EB%9E%98%EB%A8%B8%EC%8A%A4-42626-%EB%8D%94-%EB%A7%B5%EA%B2%8C-java
//참고해서 풀었음

/*
더 맵게
https://programmers.co.kr/learn/courses/30/lessons/42626?language=java
 */

public class Solution42626 {

    public static void main(String[] args) {
        Solution42626 s1 = new Solution42626();
        System.out.println(s1.solution(new int[] {1, 2, 3, 9, 10, 12},7));
    }

    public int solution(int[] scoville, int K) {
        //우선순위 큐(숫자가 낮은수부터) 정의
        PriorityQueue<Integer> pq = new PriorityQueue<>();

        //int배열을 큐에 넣어준다
        for(int i = 0; i < scoville.length; i++) {
            pq.add(scoville[i]);
        }

        int answer = 0;
        //모든 음식이 스코빌지수 K보다 커야하므로 K보다 작을때까지 반복
        while(pq.peek() < K) {
            if (pq.size() == 1) {
                return -1;
            }

            int first = pq.poll(); //가장 맵지 않은 음식
            int second = pq.poll(); //두번째로 맵지 않은 음식

            int result = first + (second * 2); //섞은음식의 스코빌지수 계산
            pq.add(result); //결과값 넣어준다
            answer++;
        }
        return answer;
    }
}