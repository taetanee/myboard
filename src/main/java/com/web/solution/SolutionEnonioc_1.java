package com.web.solution;

import java.util.Stack;

public class SolutionEnonioc_1 {

    public static void main(String[] args) {
        SolutionEnonioc_1 s1 = new SolutionEnonioc_1();
        s1.solution(new int[]{500, 1000, -300, 200, -400, 100, -100});
    }

    public int[] solution(int[] deposit) {
        Stack<Integer> stack = new Stack();
        for (int i = 0; i < deposit.length; i++) {
            if (deposit[i] > 0) {
                stack.push(deposit[i]);
            } else {
                int reminder2 = 0;
                while (true) {
                    int reminder = stack.peek() + deposit[i] - reminder2;
                    if( reminder == 0){
                        stack.pop();
                        break;
                    } else if (reminder > 0) {
                        stack.pop();
                        stack.push(reminder);
                        break;
                    } else {
                        reminder2 = stack.peek() + deposit[i];
                        stack.pop();
                    }
                }
            }
        }

        int[] answer = new int[stack.size()];
        int i = stack.size() - 1;
        while (!stack.isEmpty()) {
            answer[i] = stack.pop();
            i--;
        }
        return answer;
    }
}