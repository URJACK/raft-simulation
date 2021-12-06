package com.sicnu.netsimu.core.net;

/**
 * 网络栈数据
 * <p>
 * 例如Mac层的头信息，可以理解为是一个NetField
 * <p>
 * Ip层的头信息，也可以理解为是一个NetField
 * <p>
 * 应用层的数据字段，也可以理解为是一个NetField
 */
public interface NetField {
    String value();
}
