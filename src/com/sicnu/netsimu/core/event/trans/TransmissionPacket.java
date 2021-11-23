package com.sicnu.netsimu.core.event.trans;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 传输层数据包（简化）
 */
@Data
@AllArgsConstructor
public class TransmissionPacket {
    String srcIp;
    String desIp;
    int srcPort;
    int desPort;
    String data;
}
