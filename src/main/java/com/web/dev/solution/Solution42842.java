package com.web.dev.solution;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/*
카펫
https://school.programmers.co.kr/learn/courses/30/lessons/42842?language=java
https://dev-ku.tistory.com/283 참고해서 풀었음
 */

public class Solution42842 {
    public static void main(String[] args) {
        //Solution42842 s1 = new Solution42842();
        //s1.solution(10, 2);

        Solution42842 s2 = new Solution42842();
        s2.solution(8, 1);

        //Solution42842 s3 = new Solution42842();
        //s3.solution(24, 24);
    }


    public int[] solution(int brown, int yellow) {
        int[] answer = new int[2];

        Set<ArrayList> set = new HashSet<>();
        int temp = brown + yellow;
        for(int i=1;i<=temp;i++){
            if( temp % i == 0){
                ArrayList tempArrayList =new ArrayList();
                tempArrayList.add(i);
                tempArrayList.add(temp/i);
                set.add(tempArrayList);
            }
        }

        for(ArrayList arr : set){
            int x = (int) arr.get(1);
            int y = (int) arr.get(0);
            if( x > y ){
                continue;
            }
            if((x-2)*(y-2)==yellow){
                System.out.print(x + "," + y);
                answer[1] = x;
                answer[0] = y;
            }
        }


        return answer;
    }
}