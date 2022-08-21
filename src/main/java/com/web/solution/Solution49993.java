package com.web.solution;

/*
스킬트리
https://school.programmers.co.kr/learn/courses/30/lessons/49993
 */

import java.util.LinkedList;
import java.util.Queue;

public class Solution49993 {
    public static void main(String[] args) {
        Solution49993 s1 = new Solution49993();
        System.out.println(s1.solution("CBD",new String[]{"BACDE", "CBADF", "AECB", "BDA"}));
    }

    public int solution(String skill, String[] skill_trees) {
        int answer = 0;

        for (int i = 0; i < skill_trees.length; i++) {
            //[시작] skill로 큐 초기화
            Queue<Character> queue = new LinkedList();
            for (int j = 0; j < skill.length(); j++) {
                queue.add(skill.charAt(j));
            }
            //[종료] skill로 큐 초기화

            //[시작] 큐로 skill_trees 문자열 제거
            String tempStr = skill_trees[i];
            for (int j = 0; j < tempStr.length(); j++) {

                if (queue.peek() == null) {
                    break;
                }

                if (tempStr.charAt(j) == queue.peek()) {
                    queue.poll();
                    tempStr = tempStr.substring(0, j) + tempStr.substring(j + 1, tempStr.length());
                    j--;
                }
            }
            //[종료] 큐로 skill_trees 문자열 제거

            //[시작] skill_trees의 제거된 문자열 중 skill이 남아있는 값이 있다면, 비정상적인 스킬트리임
            boolean tempFlag = true;
            for (int j = 0; j < tempStr.length(); j++) {
                for (int z = 0; z < skill.length(); z++) {
                    if(tempStr.charAt(j) == skill.charAt(z)){
                        tempFlag = false;
                    }
                }
            }
            if( tempFlag == true){
                answer++;
            }
            //[종료] skill_trees의 제거된 문자열 중 skill이 남아있는 값이 있다면, 비정상적인 스킬트리임
        }

        return answer;
    }

//    public int solution(String skill, String[] skill_trees) {
//        int answer = 0;
//        int treeLength = skill_trees.length;
//        for(int i=0; i<treeLength; i++){
//            int skillIndex=0;
//            boolean flag = true;
//
//            int treeIdxLength = skill_trees[i].length();
//            for(int j=0; j<treeIdxLength; j++){
//                int skillLength = skill.length();
//                for(int k=skillIndex; k<skillLength; k++){
//                    if(skill.charAt(k) == skill_trees[i].charAt(j)){
//                        if(k!=skillIndex){
//                            flag = false;
//                        }else{
//                            skillIndex++;
//                        }
//
//                    }
//                }
//            }
//
//            if(flag == true){
//                answer ++;
//            }
//        }
//
//        return answer;
//    }
}
