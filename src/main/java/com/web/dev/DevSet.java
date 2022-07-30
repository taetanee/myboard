package com.web.dev;

import java.util.HashSet;
import java.util.Set;

public class DevSet {

    public static void main(String[] args) {
        Set<String> result = new HashSet<>();
        result.add("1");
        result.add("1");
        result.add("12");
        result.add("1");

        for(String value : result){
            System.out.println(value);
        }
    }
}