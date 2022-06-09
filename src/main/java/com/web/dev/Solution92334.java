package com.web.dev;

import java.util.HashMap;

public class Solution92334 {

    public static void main(String[] args) {
        Solution92334 s1 = new Solution92334();
        System.out.println(s1.solution(new String[] {"muzi", "frodo", "apeach", "neo"},new String[]{"muzi frodo","apeach frodo","frodo neo","muzi neo","apeach muzi"}, 2 ));

        Solution92334 s2 = new Solution92334();
        System.out.println(s2.solution(new String[] {"con", "ryan"},new String[]{"ryan con", "ryan con", "ryan con", "ryan con"}, 3));
    }

    public int[] solution(String[] id_list, String[] report, int k) {
        int[] answer = {};

        HashMap<String, HashMap<String,String>> tempReport = new HashMap<>();

        //[시작] tempReport 초기화
        for(int i=0;i<id_list.length;i++){
            tempReport.put(id_list[i]);
        }
        //[종료] tempReport 초기화

        String reporter = "";
        String reported = "";
        String [] x2 = new String[2];
        for (int i=0;i<report.length;i++){
            x2 = report[i].split(" ");
            reporter = x2[0];
            reported = x2[1];

            tempReport.put()


        }



        return answer;
    }
}
