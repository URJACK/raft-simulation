package com.sicnu.raftsimu.core.event;

import com.sicnu.raftsimu.core.event.trans.TransmissionPacket;
import com.sicnu.raftsimu.core.mote.Mote;
import lombok.Data;

@Data
public class TransmissionEvent extends Event {

    Mote receiver;
    TransmissionPacket packet;

    public TransmissionEvent(long triggerTime, Mote receiver, TransmissionPacket packet) {
        super(triggerTime);
        this.receiver = receiver;
        this.packet = packet;
    }

    @Override
    public void work() {
        receiver.netReceive(packet);
    }
}
