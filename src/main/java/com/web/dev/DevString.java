package com.web.dev;

public class DevString {

    //됨 (이 두가지를 제외하고 나머지는 안됨)
    //public static void main(String args[]){

    //됨 (이 두가지를 제외하고 나머지는 안됨)
    static public void main(String args[]){

        String x = "a";
        String y = "a";

        if( x == y ){
            System.out.println("if");
            System.out.println("이 쪽이 호출되는 이유는 같은 힙 메모리를 참조하기 때문");
        } else {
            System.out.println("else");
        }

    }
}
