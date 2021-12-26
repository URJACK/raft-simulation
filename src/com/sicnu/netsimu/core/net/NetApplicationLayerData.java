package com.sicnu.netsimu.core.net;

/**
 * 应用层数据
 */
public class NetApplicationLayerData implements NetField {
    byte[] value;

    public NetApplicationLayerData(byte[] value) {
        this.value = value;
    }

    @Override
    public byte[] value() {
        return value;
    }
}
