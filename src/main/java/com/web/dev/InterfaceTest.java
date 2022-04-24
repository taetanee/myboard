package com.web.dev;

public class InterfaceTest {

    public static void main(String[] args) {
        C1 c1 = new C1();
        System.out.println(c1.f1());
    }

}

class C1 implements I1{
    @Override
    public int f1() {
        return 0;
    }
}

interface I1{
    int f1();
}