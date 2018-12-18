package com.credorax;

import java.io.IOException;

public class Application {
    public static void main(String[] args) throws IOException {
        CallsLogLoadAnalyser callsLogLoadAnalyser =  new CallsLogLoadAnalyser("source.txt");
//        CallsLogLoadAnalyser callsLogLoadAnalyser =  new CallsLogLoadAnalyser(this.getClass().getClassLoader().getResource("source.txt").);
        callsLogLoadAnalyser.processCalls();
    }
}
