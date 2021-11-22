package com.sicnu.raftsimu.core.event;

import lombok.Data;

@Data
public abstract class Event implements Comparable<Event> {
    protected long triggerTime;

    public Event() {
    }

    public Event(long triggerTime) {
        this.triggerTime = triggerTime;
    }

    public abstract void work();

    @Override
    public int compareTo(Event o) {
        return (int) (this.triggerTime - o.triggerTime);
    }
}