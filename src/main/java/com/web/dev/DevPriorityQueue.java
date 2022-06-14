package com.web.dev;

import java.util.PriorityQueue;

public class DevPriorityQueue {
    public static void main(String[] args) {

        /* [시작] Priority Queue(우선순위 큐) 개념과 동작( https://crazykim2.tistory.com/575 )  */
        PriorityQueue<Integer> pq = new PriorityQueue<>();
        pq.add(1);
        pq.add(15);
        pq.offer(10);
        pq.add(21);
        pq.add(25);
        pq.offer(18);
        pq.add(8);
        /* [종료] Priority Queue(우선순위 큐) 개념과 동작( https://crazykim2.tistory.com/575 )  */


        /* [시작] add와 offer 차이 ( https://goodteacher.tistory.com/112 )  */
        PriorityQueue<Integer> pq2 = new PriorityQueue<>();
        pq2.add(1);
        pq2.poll(); //1 반환
        pq2.poll(); //null반환(에러 발생 안함)

        PriorityQueue<Integer> pq3 = new PriorityQueue<>();
        pq3.add(1);
        pq3.remove();   //1 반환
        pq3.remove();   //예외 발생
        /* [종료] add와 offer 차이 ( https://goodteacher.tistory.com/112 )  */

        System.out.println("끝");
    }
}
