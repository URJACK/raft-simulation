package com.sicnu.raftsimu.core.event.trans;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TransmissionPacket {
    String srcIp;
    String desIp;
    int srcPort;
    int desPort;
    String data;
}
