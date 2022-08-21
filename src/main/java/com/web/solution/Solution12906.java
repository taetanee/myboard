package com.web.solution;

/*
같은 숫자는 싫어
https://school.programmers.co.kr/learn/courses/30/lessons/12906
 */

import java.util.Iterator;
import java.util.Stack;

public class Solution12906 {
    public static void main(String[] args) {
        Solution12906 s1 = new Solution12906();
        System.out.println(s1.solution(new int[] {1,1,3,3,0,1,1}));

        Solution12906 s2 = new Solution12906();
        System.out.println(s2.solution(new int[] {4,4,4,3,3}));
    }


    public int[] solution(int []arr) {

        Stack<Integer> queue = new Stack();
        for(int i=0;i<arr.length;i++){
            if( i == 0 ){
                queue.add(arr[i]);
            } else if( queue.peek() != arr[i] ){
                queue.add(arr[i]);
            }
        }

        int[] answer = new int[queue.size()];
        Iterator<Integer> itr = queue.iterator();
        int i = 0;
        while (itr.hasNext()) {
            answer[i] = itr.next();
            i++;
        }

        return answer;
    }
}
