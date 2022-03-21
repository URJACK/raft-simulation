package com.sicnu.nettest.core.net.channel;

public class ThrowExceptionTest {
    public static void main(String[] args) {
        int a = 5;
        if (a != 5) {
            throw new RuntimeException("No Answer Here");
        }
    }
}
