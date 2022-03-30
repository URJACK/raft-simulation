package com.sicnu.nettest.core.net.channel;

import com.sicnu.netsimu.core.net.mac.channel.Signal;
import com.sicnu.netsimu.core.net.mac.channel.Channel;

import java.util.ArrayList;

public class ChannelTest {
    public static void main(String[] args) {
        Signal a = new Signal(0, 100, "n".getBytes(), null);
        Signal b = new Signal(100, 150, "n".getBytes(), null);
        Signal c = new Signal(120, 140, "n".getBytes(), null);
        Signal d = new Signal(50, 30, "n".getBytes(), null);
        Signal e = new Signal(50, 200, "n".getBytes(), null);
        Signal f = new Signal(50, 100, "n".getBytes(), null);
        ArrayList<Signal> list = new ArrayList<>();
        Channel.insertSortWithList(list, a);
        Channel.insertSortWithList(list, b);
        Channel.insertSortWithList(list, c);
        Channel.insertSortWithList(list, d);
        Channel.insertSortWithList(list, e);
        Channel.insertSortWithList(list, f);
        System.out.println(list);
    }
}
