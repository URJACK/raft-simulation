package com.sicnu.netsimu.core.net.ip;

import com.sicnu.netsimu.core.net.NetField;
import com.sicnu.netsimu.core.net.NetLayer;
import com.sicnu.netsimu.exception.ParseException;

public class BasicIPLayer extends NetLayer {
    /**
     * 存储的Ip层的网络地址
     */
    byte[] ipAddress;

    /**
     * 网络栈将传入的若干参数包装成字符串
     *
     * @param arg
     * @return 包装成的字符串。
     */
    @Override
    public byte[] convert(NetField arg) throws Exception {
        return null;
    }

    /**
     * 解析字符串
     *
     * @param str NetStack分发给自己的，还没有进行解析的字符串数据
     * @return 解析后的协议的头信息或者是应用层数据
     * @throws ParseException 解析异常
     */
    @Override
    public NetField parse(byte[] packet) throws ParseException {
        return null;
    }


    /**
     * 验证该层是否可以接收这个数据包
     *
     * @param headerStr 协议头字段
     * @return
     */
    @Override
    public boolean validate(byte[] head) {
        return false;
    }


    /**
     * 设置Ip层的网络地址
     *
     * @param value Ip层，网络地址的值
     */
    public void setIpSourceAddress(byte[] value) {
        ipAddress = value;
    }

    /**
     * 取得设置后的Ip层的网络地址
     *
     * @return Ip层的网络地址
     */
    public byte[] getIpSourceAddress() {
        return ipAddress;
    }

    public static class Header{
        public static final int length = 0;
    }
}
