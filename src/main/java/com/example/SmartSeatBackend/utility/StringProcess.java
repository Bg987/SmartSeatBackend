package com.example.SmartSeatBackend.utility;

public class StringProcess {

    public static String process(String fullMessage){
        int start = fullMessage.indexOf("=") + 2;
        int end = fullMessage.indexOf("com") + "exists".length()-1;
        return fullMessage.substring(start, end)+" already exist";
    }
}
