package com.web.dev;

import java.util.PriorityQueue;

/*
https://crazykim2.tistory.com/575
 */
public class DevPriorityQueue {
    public static void main(String[] args) {

        PriorityQueue<Integer> pq = new PriorityQueue<>();
        pq.add(1);
        pq.add(15);
        pq.offer(10);
        pq.add(21);
        pq.add(25);
        pq.offer(18);
        pq.add(8);


        /* [시작] add와 offer 차이 ( https://goodteacher.tistory.com/112 )  */
        PriorityQueue<Integer> pq2 = new PriorityQueue<>();
        pq2.add(1);
        pq2.poll();
        pq2.poll();

        PriorityQueue<Integer> pq3 = new PriorityQueue<>();
        pq3.add(1);
        pq3.remove();
        pq3.remove();
        /* [종료] add와 offer 차이 ( https://goodteacher.tistory.com/112 )  */

        System.out.println("끝");
    }
}
