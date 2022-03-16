package com.sicnu.netsimu.core.net.mac;

import com.sicnu.netsimu.core.net.NetField;
import com.sicnu.netsimu.core.net.NetLayer;
import com.sicnu.netsimu.core.node.Node;
import com.sicnu.netsimu.exception.ParseException;
import lombok.Data;

import java.util.Arrays;

/**
 * MAC层，继承自NetLayer
 * NetLayer与Mote互相持有引用
 * <p>
 * MACLayer层，存储了自身的mac地址
 *
 * @see Node
 * @see NetLayer
 */
public class BasicMACLayer extends NetLayer {
    /**
     * 广播地址
     */
    public static final byte[] BROAD_CAST = {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
            (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
//    public static final String BROAD_CAST = "FF:FF:FF:FF:FF:FF";
    /**
     * 网络栈中存储的MAC层地址
     */
    private byte[] macAddress;

    /**
     * 构建MAC层对象
     *
     * @param macAddress MAC层地址
     */
    public BasicMACLayer(byte[] macAddress) {
        this.macAddress = macAddress;
    }

    /**
     * 将一个MAC层头对象转为需要传输的字符串
     *
     * @param args MAC层头对象
     * @return "AE:FF:35:0B:95:85,AE:FF:35:0B:95:85"
     * @throws Exception
     */
    @Override
    public byte[] convert(NetField args) throws Exception {
        if (args instanceof Header) {
            Header header = (Header) args;
            return header.value();
        }
        throw new Exception("Not MAC Frame");
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
        byte[] src = new byte[6];
        byte[] dst = new byte[6];
        System.arraycopy(packet, 0, src, 0, 6);
        System.arraycopy(packet, 6, dst, 0, 6);
        return new Header(src, dst);
    }

    /**
     * 验证该层是否可以接收这个数据包
     *
     * @param headerStr 协议头字段
     * @return
     */
    @Override
    public boolean validate(byte[] header) {
        try {
            Header head = (Header) parse(header);
            if (Arrays.equals(head.getDes(), BROAD_CAST)) {
                //如果是广播地址
                return true;
            }
            //如果等于自己的Mac地址
            return Arrays.equals(head.getDes(), macAddress);
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public int getHeaderLength() {
        return 12;
    }

    /**
     * 返回MAC层地址
     *
     * @return MAC层地址
     */
    public byte[] getMacSourceAddress() {
        return macAddress;
    }

    /**
     * 设置Mac源地址
     *
     * @param address mac源地址
     */
    public void setMacSourceAddress(byte[] address) {
        macAddress = address;
    }

    /**
     * MAC层头信息
     */
    @Data
    public static class Header implements NetField {
        byte[] src;
        byte[] des;

        /**
         * @param src 源Mac地址
         * @param des 目标Mac地址
         * @throws ParseException Mac地址转换异常
         */
        public Header(byte[] src, byte[] des) throws ParseException {
            if (validateAddressFormat(src) || validateAddressFormat(des)) {
                throw new ParseException("MAC头信息转换错误");
            }
            this.src = src;
            this.des = des;
        }

        /**
         * 比如形如 "AE:FF:35:0B:95:85"
         *
         * @param address 即将被校验的MAC地址
         * @return true==合法校验
         */
        private boolean validateAddressFormat(byte[] address) {
            //没有6个子字段
            return address.length != 6;
        }

        @Override
        public byte[] value() {
            byte[] bytes = new byte[12];
            System.arraycopy(src, 0, bytes, 0, 6);
            System.arraycopy(des, 0, bytes, 6, 6);
            return bytes;
        }
    }
}
