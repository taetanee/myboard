package com.web.dev;

/*
소수 찾기
https://school.programmers.co.kr/learn/courses/30/lessons/42839?language=java
참고해서 풀었음 : https://tmdrl5779.tistory.com/213
 */

import java.util.HashSet;
import java.util.Set;

public class Solution42839 {

    static Set<Integer> set = new HashSet<>();

    public static void main(String[] args) {
        Solution42839 s1 = new Solution42839();
        s1.solution("123");
    }

    public int solution(String numbers) {
        int answer = 0;

        dfs(numbers, new boolean[numbers.length()], 0, new StringBuilder());

        for (Integer num : set){
            System.out.println(num);
        }

        return answer;
    }

    public void dfs(String numbers, boolean[] visited, int depth, StringBuilder s){
        for(int i = 0; i < visited.length; i++){
            if(visited[i] == false) {
                visited[i] = true;
                s.append(numbers.charAt(i));
                set.add(Integer.parseInt(s.toString()));
                dfs(numbers, visited, depth+1, s);
                s.deleteCharAt(s.length()-1);
                visited[i] = false;
            }
        }
    }
}