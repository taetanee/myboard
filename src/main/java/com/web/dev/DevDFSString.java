package com.web.dev;

/*
소수 찾기
https://school.programmers.co.kr/learn/courses/30/lessons/42839?language=java
 */

import java.util.HashSet;
import java.util.Set;

public class DevDFSString {

    static Set<Integer> result = new HashSet<>();

    public static void main(String[] args) {

        DevDFSString s1 = new DevDFSString();
        s1.solution("17");

        DevDFSString s2 = new DevDFSString();
        s2.solution("011");

    }

    public int solution(String numbers) {
        int answer = 0;
        int length = numbers.length();
        char[] output = new char[length];
        boolean[] visited = new boolean[length];
        dfs(numbers,output, visited, 0);

        for(int value : result) {
            System.out.println(value);
        }
        return answer;
    }

    public void dfs(String s, char[] output, boolean[] visited, int depth){

        int length = s.length();

        if(length == depth){
            result.add(Integer.parseInt(String.valueOf(output)));
            return;
        }

        for(int i=0;i<length;i++){
            if (visited[i] == false) {
                visited[i] = true;
                output[depth] = s.charAt(i);
                dfs(s, output, visited, depth + 1);
                visited[i] = false;
            }
        }
    }


}