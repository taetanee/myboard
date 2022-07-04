import java.util.Stack;

/*
짝지어 제거하기
https://programmers.co.kr/learn/courses/30/lessons/12973
(정답보고 풀었음)
 */

public class Solution12973 {

    public static void main(String[] args) {
        Solution12973 s1 = new Solution12973();
        s1.solution("baabaa");

        //Solution12973 s2 = new Solution12973();
        //s2.solution("cdcd");
    }

    public int solution(String s){
        int answer = 0;

        Stack<Character> stack = new Stack();

        for(char c : s.toCharArray()){
            if( stack.size() == 0 ){
                stack.push(c);
            } else if( stack.peek() == c ){
                stack.pop();
            } else {
                stack.push(c);
            }
        }

        if( stack.empty() ){
            answer = 1;
        } else {
            answer = 0;
        }
        return answer;
    }
}
