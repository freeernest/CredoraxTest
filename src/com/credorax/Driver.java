package com.credorax;

public class Driver {
    public static void main(String[] args) {
        CallsLogLoadAnalyser callsLogLoadAnalyser =  new CallsLogLoadAnalyser("resources/source.txt","resources/result.txt");
        callsLogLoadAnalyser.processCalls();
        callsLogLoadAnalyser.FindMaxInterval();
    }
}
