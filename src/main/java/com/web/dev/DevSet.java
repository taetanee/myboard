package com.web.dev;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class DevSet {

    public static void main(String[] args) {
        Set<String> set1 = new HashSet<>();
        set1.add("1");
        set1.add("1");
        set1.add("12");
        set1.add("1");

        for(String value : set1){
            System.out.println(value);
        }
        System.out.println("======");

        Set<ArrayList> set2 = new HashSet<>();
        ArrayList arrayList1 = new ArrayList();
        arrayList1.add("1");
        arrayList1.add("2");
        set2.add(arrayList1);

        ArrayList arrayList2 = new ArrayList();
        arrayList2.add("1");
        arrayList2.add("2");
        set2.add(arrayList2);

        ArrayList arrayList3 = new ArrayList();
        arrayList3.add("99");
        arrayList3.add("99");
        set2.add(arrayList3);

        for(ArrayList value : set2){
            for(int i=0;i<value.size();i++){
                System.out.print(value.get(i));
                System.out.print(",");
            }
            System.out.println();
        }
    }
}