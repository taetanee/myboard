package com.web.solution;

/*
[1차 캐시]
https://school.programmers.co.kr/learn/courses/30/lessons/17680
 */

import java.util.LinkedList;

public class Solution17680 {
    public static void main(String[] args) {

//        Solution17680 s1 = new Solution17680();
//        System.out.println(s1.solution(3, new String[] {"Jeju", "Pangyo", "Seoul", "NewYork", "LA", "Jeju", "Pangyo", "Seoul", "NewYork", "LA"}));//50
//        Solution17680 s2 = new Solution17680();
//        System.out.println(s2.solution(3, new String[] {"Jeju", "Pangyo", "Seoul", "Jeju", "Pangyo", "Seoul", "Jeju", "Pangyo", "Seoul"}));//21
//        Solution17680 s3 = new Solution17680();
//        System.out.println(s3.solution(2, new String[] {"Jeju", "Pangyo", "Seoul", "NewYork", "LA", "SanFrancisco", "Seoul", "Rome", "Paris", "Jeju", "NewYork", "Rome"}));//60
//        Solution17680 s4 = new Solution17680();
//        System.out.println(s4.solution(5, new String[] {"Jeju", "Pangyo", "Seoul", "NewYork", "LA", "SanFrancisco", "Seoul", "Rome", "Paris", "Jeju", "NewYork", "Rome"}));//52
//        Solution17680 s5 = new Solution17680();
//        System.out.println(s5.solution(2, new String[] {"Jeju", "Pangyo", "NewYork", "newyork"}));//16
//        Solution17680 s6 = new Solution17680();
//        System.out.println(s6.solution(0, new String[] {"Jeju", "Pangyo", "Seoul", "NewYork", "LA"}));//25

        //Solution17680 s7 = new Solution17680();
        //System.out.println(s7.solution(2, new String[] {"A", "A", "A", "A", "A"}));//9
//        Solution17680 s8 = new Solution17680();
//        System.out.println(s8.solution(3, new String[] {"A", "B", "A"}));//11

        Solution17680 s9 = new Solution17680();
        System.out.println(s9.solution(4, new String[] {"1", "2", "3", "1", "4", "5"}));//11
    }
    public int solution(int cacheSize, String[] cities) {
        int answer = 0;

        LinkedList<String> ll = new LinkedList<String>();
        for(int i=0; i < cities.length ; i++){
            String cityName = cities[i].toUpperCase();

            boolean isCacheHit = false;
            for(int j=0;j<ll.size();j++){
                if(cityName.equals(ll.get(j).toUpperCase())){
                    isCacheHit = true;
                }
            }
            if( cacheSize == 0){
                answer += 5;
            } else if( isCacheHit == true && ll.size() < cacheSize){
                ll.add(cityName);
                ll.remove(getIndexByKey(cityName,ll));
                answer += 1;
            } else if( isCacheHit == true && ll.size() == cacheSize){
                ll.remove(0);
                ll.add(cityName);
                answer += 1;
            } else if( isCacheHit == false && ll.size() < cacheSize){
                ll.add(cityName);
                answer += 5;
            } else if( isCacheHit == false && ll.size() == cacheSize){
                ll.remove(0);
                ll.add(cityName);
                answer += 5;
            } else {
                System.out.println("예기치 않은 오류 발생");
            }
            System.out.println("");
        }

        return answer;
    }

    public int getIndexByKey( String cityName , LinkedList<String> ll){
        for(int i=0;i<ll.size();i++){
            if( cityName.equals(ll.get(i))){
                return i;
            }
        }
        return -1;
    }
}
