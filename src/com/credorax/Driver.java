package com.credorax;

import java.io.IOException;

public class Driver {
    public static void main(String[] args) throws IOException {
        CallsLogLoadAnalyser callsLogLoadAnalyser =  new CallsLogLoadAnalyser("resources/source.txt");
        callsLogLoadAnalyser.processCalls();
    }
}
