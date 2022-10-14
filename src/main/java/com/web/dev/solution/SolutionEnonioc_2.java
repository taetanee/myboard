package com.web.dev.solution;

import java.util.*;

public class SolutionEnonioc_2 {

    public static void main(String[] args) {
        SolutionEnonioc_2 s1 = new SolutionEnonioc_2();
        s1.solution(new int[][] {{1,0,5},{2,2,2},{3,3,1},{4,4,1},{5,10,2}}); //1,3,4,2,5

        //SolutionEnonioc_2 s2 = new SolutionEnonioc_2();
        //s2.solution(new int[][] {{1,0,2},{2,1,3},{3,3,2},{4,9,1},{5,10,2}}); //1,3,2,4,5
    }
    public int[] solution(int[][] data){
        ArrayList<Integer> tempAnswer = new ArrayList();

        Queue<HashMap<Integer,Integer>> queue = new LinkedList<>();
        HashMap<Integer,Integer> current = new HashMap();

        int time = 0;
        while( tempAnswer.size() != data.length ){
            //[시작] 시간마다 요청 찾기
            for(int i=0; i<data.length; i++){
                if( data[i][1] == time ){
                    HashMap<Integer,Integer> temp = new HashMap();
                    temp.put(time, data[i][0]);
                    queue.add(temp);
                }
            }
            //[종료] 시간마다 요청 찾기

            time++;
        }

        int[] answer = {};
        return answer;
    }

}