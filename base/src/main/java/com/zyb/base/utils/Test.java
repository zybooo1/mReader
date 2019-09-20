package com.zyb.base.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {
    public static void main(String[] args) {

        String str="2342344324ewrwerwdfsuykjkl";
        System.out.println(isContainChinese(str));
        String str2="2342344你好啊324ewrwerwdfsuykjkl哈哈哈";
        System.out.println(isContainChinese(str2));
        String str3="怎呃了咯几级";
        System.out.println(isContainChinese(str3));
    }

    public static boolean isContainChinese(String str) {
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(str);
        return m.find();
    }
}
