package com.sicnu.netsimu.core.net;

/**
 * 应用层数据
 */
public class NetApplicationLayerData implements NetField {
    String value;

    public NetApplicationLayerData(String value) {
        this.value = value;
    }

    @Override
    public String value() {
        return value;
    }
}
