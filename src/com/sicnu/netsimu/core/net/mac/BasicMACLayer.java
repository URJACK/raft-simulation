package com.sicnu.netsimu.core.net.mac;

import com.sicnu.netsimu.core.net.NetField;
import com.sicnu.netsimu.core.net.NetLayer;
import com.sicnu.netsimu.exception.ParseException;
import lombok.Data;

/**
 * MAC层，继承自NetLayer
 * NetLayer与Mote互相持有引用
 * <p>
 * MACLayer层，存储了自身的mac地址
 *
 * @see com.sicnu.netsimu.core.mote.Mote
 * @see NetLayer
 */
public class BasicMACLayer extends NetLayer {
    /**
     * 广播地址
     */
    public static final String BROAD_CAST = "FF:FF:FF:FF:FF:FF";
    /**
     * 网络栈中存储的MAC层地址
     */
    private String macAddress;

    /**
     * 构建MAC层对象
     *
     * @param macAddress MAC层地址
     */
    public BasicMACLayer(String macAddress) {
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
    public String convert(NetField args) throws Exception {
        if (args instanceof Header) {
            Header header = (Header) args;
            return header.getSrc() + "," + header.getDes();
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
    public NetField parse(String str) throws ParseException {
        String[] splits = str.split(",");
        Header header = new Header(splits[0], splits[1]);
        return header;
    }

    /**
     * 验证该层是否可以接收这个数据包
     *
     * @param headerStr 协议头字段
     * @return
     */
    @Override
    public boolean validate(String headerStr) {
        try {
            Header header = (Header) parse(headerStr);
            if (header.getDes().equals(BROAD_CAST)) {
                //如果是广播地址
                return true;
            }
            if (header.getDes().equals(macAddress)) {
                //如果等于自己的Mac地址
                return true;
            }
            return false;
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 返回MAC层地址
     *
     * @return MAC层地址
     */
    public String getMacSourceAddress() {
        return macAddress;
    }

    /**
     * 设置Mac源地址
     *
     * @param address mac源地址
     */
    public void setMacSourceAddress(String address) {
        macAddress = address;
    }

    /**
     * MAC层头信息
     */
    @Data
    public static class Header implements NetField {
        String src;
        String des;

        /**
         * @param src 源Mac地址
         * @param des 目标Mac地址
         * @throws ParseException Mac地址转换异常
         */
        public Header(String src, String des) throws ParseException {
            if (!validateAddressLength(src) || !validateAddressLength(des)) {
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
        private boolean validateAddressLength(String address) {
            String[] splits = address.split(":");
            if (splits.length != 6) {
                //没有6个子字段
                return false;
            }
            for (int i = 0; i < 6; i++) {
                String str = splits[i];
                if (str.length() != 2) {
                    //单字段长度错误
                    return false;
                }
            }
            return true;
        }

        @Override
        public String value() {
            return src + ":" + des;
        }
    }
}
