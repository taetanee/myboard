package com.web.dev;

import ch.qos.logback.core.encoder.EchoEncoder;

import java.util.NoSuchElementException;
import java.util.PriorityQueue;

public class DevPriorityQueue {
    public static void main(String[] args) {

        //[시작] Priority Queue(우선순위 큐) 개념과 동작( https://crazykim2.tistory.com/575 )
        PriorityQueue<Integer> pq = new PriorityQueue<>();
        pq.add(1);
        pq.add(15);
        pq.offer(10);
        pq.add(21);
        pq.add(25);
        pq.offer(18);
        pq.add(8);
        //[종료] Priority Queue(우선순위 큐) 개념과 동작( https://crazykim2.tistory.com/575 )

        //[시작] String에서도 알파벳 순으로 됨
        PriorityQueue<String> pqs = new PriorityQueue<>();
        pqs.add("aa");
        pqs.add("oo");
        pqs.offer("jj");
        pqs.add("uu");
        pqs.add("yy");
        pqs.offer("rr");
        pqs.add("hh");
        //[종료] String에서도 알파벳 순으로 됨


        // [시작] add와 offer 차이 ( https://goodteacher.tistory.com/112 )
        PriorityQueue<Integer> pq2 = new PriorityQueue<>();
        pq2.add(1);
        pq2.poll(); //1 반환
        pq2.poll(); //null반환(에러 발생 안함)

        try {
            PriorityQueue<Integer> pq3 = new PriorityQueue<>();
            pq3.add(1);
            pq3.remove();   //1 반환
            pq3.remove();   //예외 발생
        } catch (NoSuchElementException e){
            System.out.println("NoSuchElementException > error");
        } catch (Exception e){
            System.out.println("Exception > error");
        }
        // [종료] add와 offer 차이 ( https://goodteacher.tistory.com/112 )

        System.out.println("끝");
    }
}
