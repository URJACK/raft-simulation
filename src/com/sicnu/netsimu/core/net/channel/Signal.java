package com.sicnu.netsimu.core.net.channel;

import com.sicnu.netsimu.core.event.Event;
import com.sicnu.netsimu.core.event.MACSignalEvent;

public class Signal implements Comparable<Signal> {
    long begin;
    long end;
    boolean isConflicted;
    byte[] data;
    /**
     * Each Signal Will point at one Event(its time is )
     */
    MACSignalEvent event;

    public Signal(long begin, long end, byte[] data, MACSignalEvent event) {
        this.begin = begin;
        this.end = end;
        this.data = data;
        this.event = event;
        this.isConflicted = false;
    }

    public boolean isConflicted() {
        return isConflicted;
    }

    public void conflict() {
        isConflicted = true;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public int compareTo(Signal o) {
        return (int) (this.end - o.end);
    }

    @Override
    public String toString() {
        return "Signal{" +
                "begin=" + begin +
                ", end=" + end +
                '}';
    }
}