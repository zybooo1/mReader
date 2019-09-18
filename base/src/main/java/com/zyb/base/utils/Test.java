package com.zyb.base.utils;

public class Test {
    public static void main(String[] args) {
        String str="234  234 4324";
        System.out.println(str);
       String s= str.replaceAll("\\s*", "");
        System.out.println(str);
        System.out.println(s);
    }
}
