package com.web.solution;

/*
신규 아이디 추천
https://school.programmers.co.kr/learn/courses/30/lessons/72410
 */

public class Solution72410 {
    public static void main(String[] args) {
        Solution72410 s1 = new Solution72410();
        //s1.solution("...!@BaT#*..y.abcdefghijklm");//bat.y.abcdefghi
        //s1.solution("z-+.^.");//	"z--");
        //s1.solution("=.=");//	"aaa");
        //s1.solution("123_.def");//	"123_.def");
        s1.solution("abcdefghijklmn.p");//	"abcdefghijklmn");
    }
    public String solution(String new_id) {
        String answer = "";
        String temp = "";

        temp = new_id.toLowerCase();

        temp = temp.replaceAll("[^a-z0-9-_.]", "");

        temp = temp.replaceAll("[.]{2,}", ".");

        if(temp.charAt(0) == '.'){
            temp = temp.substring(1);
        }

        if(temp.length()-1 >= 0 &&temp.charAt(temp.length()-1) == '.'){
            temp = temp.substring(0,temp.length()-1);
        }

        if("".contentEquals(temp)){
            temp = "a";
        }

        //temp = temp.trim();

        if( temp.length() >=  16 ){
            temp = temp.substring(0,15);

            if(temp.charAt(temp.length()-1) == '.'){
                temp = temp.substring(0,temp.length()-1);
            }
        }

        if(temp.length() == 1){
            temp = temp + temp.charAt(temp.length()-1) + temp.charAt(temp.length()-1);
        } else if(temp.length() == 2){
            temp = temp + temp.charAt(temp.length()-1);
        } else {
            //no action
        }
        answer = temp;

        return answer;
    }
}
