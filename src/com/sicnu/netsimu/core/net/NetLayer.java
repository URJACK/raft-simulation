package com.sicnu.netsimu.core.net;

import com.sicnu.netsimu.exception.ParseException;

public abstract class NetLayer {
    /**
     * 网络栈将传入的若干参数包装成字符串
     *
     * @param args 传入的参数 -> 各层的头信息和最后的应用层数据
     * @return 包装成的字符串。
     */
    public abstract byte[] convert(NetField arg) throws Exception;

    /**
     * 解析字符串
     *
     * @param str NetStack分发给自己的，还没有进行解析的字符串数据
     * @return 解析后的协议的头信息或者是应用层数据
     * @throws ParseException 解析异常
     */
    public abstract NetField parse(byte[] packet) throws ParseException;

    /**
     * 验证该层是否可以接收这个数据包
     *
     * @param headerStr 协议头字段
     * @return
     */
    public abstract boolean validate(byte[] header);

    /**
     * 返回当前协议的头字节长度
     *
     * @return 协议的头字节长度
     */
    public abstract int getHeaderLength();
}
