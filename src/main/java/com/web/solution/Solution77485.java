package com.web.solution;

/*
문제제에에목
https://programmers.co.kr/learn/courses/30/lessons/xxxxx
 */

public class Solution77485 {
    public static void main(String[] args) {

        Solution77485 s1 = new Solution77485();
        System.out.println(s1.solution(6,6, new int[][] {{2,2,5,4}}));
        //System.out.println(s1.solution(6,6, new int[][] {{2,2,5,4},{3,3,6,6},{5,1,6,3}}));
    }

    public int[] solution(int rows, int columns, int[][] queries) {
        int[] answer = {};
        int[][] matrix = new int[rows][columns];

        //[시작] 배열 초기화
        for(int i=0;i<rows;i++){
            for(int j=0;j<columns;j++) {
                matrix[i][j] = i * columns + j + 1;
            }
        }
        //[종료] 배열 초기화

        //[시작] 회전 시작
        int min = 0;
        int temp1 = 0;
        int temp2 = 0;
        for(int i=0;i<queries.length;i++){

            //[시작] 첫번재 시계방향
            for(int xi =queries[i][1]-1; xi < queries[i][3] ; xi++){
                int currentRow = queries[i][1]-1;
                int currentCol = xi;
                System.out.println(currentRow + ","+(currentCol));
                if( xi == currentRow){
                    temp1 = matrix[currentRow][currentCol];
                    continue;
                }

                temp2 = matrix[currentRow][currentCol];
                matrix[currentRow][currentCol] = temp1;
                temp1 = temp2;
            }
            //[종료] 첫번재 시계방향


        }
        //[종료] 회전 시작

        printArray(matrix);

        return answer;
    }

    public void printArray(int[][] arr){

        System.out.println("======printArray======");
        for(int i=0;i<arr.length;i++){
            for(int j=0;j<arr.length;j++) {
                System.out.print(arr[i][j]+", ");
            }
            System.out.println("");
        }
        System.out.println("======printArray======");
    }
}
