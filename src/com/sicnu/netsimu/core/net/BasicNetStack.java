package com.sicnu.netsimu.core.net;

import com.sicnu.netsimu.core.net.ip.BasicIPLayer;
import com.sicnu.netsimu.core.net.mac.IEEE_802_11_MACLayer;
import com.sicnu.netsimu.core.utils.MoteCalculate;
import com.sicnu.netsimu.exception.ParseException;

import java.util.ArrayList;

/**
 * 基础网络栈：目前包含仅包含Mac层，写了一个Ip层占位。
 * NetStack 的 parse() 与 convert() 本质上就是不断调用 NetLayer的 同名函数
 *
 * @see NetLayer
 * @see IEEE_802_11_MACLayer
 */
public class BasicNetStack extends NetStack {
    IEEE_802_11_MACLayer macLayer;
    BasicIPLayer ipLayer;
    int stackHeaderLength;

    /**
     * 基础网络栈构造函数
     *
     * @param macAddress 当前设备的MAC地址
     */
    public BasicNetStack(byte[] macAddress) {
        macLayer = new IEEE_802_11_MACLayer(macAddress);
        ipLayer = new BasicIPLayer();
        stackHeaderLength = IEEE_802_11_MACLayer.Header.HEADER_LENGTH + BasicIPLayer.Header.length;
    }

    /**
     * 网络栈将传入的各类Header和数据转换成可以传输的字节对象
     * <pre>
     *     return convert(data, macHeader, ipHeader);
     * </pre>
     *
     * @param value 要传输的应用层数据内容
     * @param args  Header与传输的数据
     * @return 可传输字节对象
     */
    @Override
    public byte[] convert(byte[] value, NetField... args) {
        try {
            byte[] macHeader = macLayer.convert(args[0]);
            byte[] valueByte = value;
            byte[] data = new byte[macHeader.length + valueByte.length];
            System.arraycopy(macHeader, 0, data, 0, macHeader.length);
            System.arraycopy(valueByte, 0, data, macHeader.length, valueByte.length);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 通过网络栈检查自己是否可以接收该数据包
     * packet 的字段按 MAC -> IP -> ... -> APPLICATION 进行排序
     *
     * @param packet 传输来的数据包
     * @return null==无法接收该数据包
     */
    @Override
    public ArrayList<NetField> parse(byte[] packet) {
        if (packet.length < 12) {
            new ParseException("数据包字段数不匹配").printStackTrace();
            return null;
        }
        // mac层头信息的对应数组
        byte[] macHeaderBytes = new byte[IEEE_802_11_MACLayer.Header.HEADER_LENGTH];
        // 开除mac层头信息后的信息长度，并以该长度创建对应数组
        int valueLength = packet.length - IEEE_802_11_MACLayer.Header.HEADER_LENGTH;
        byte[] value = new byte[valueLength];
        // 将内容分别塞入这些部分
        System.arraycopy(packet, 0, macHeaderBytes, 0, IEEE_802_11_MACLayer.Header.HEADER_LENGTH);
        System.arraycopy(packet, IEEE_802_11_MACLayer.Header.HEADER_LENGTH, value, 0, valueLength);
        try {
            ArrayList<NetField> ans = new ArrayList<>(2);
            if (!macLayer.validate(macHeaderBytes)) {
                return null;
            }
            NetField macHeader = macLayer.parse(macHeaderBytes);
            NetField data = new NetApplicationLayerData(value);
            ans.add(macHeader);
            ans.add(data);
            return ans;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 从网络栈中取得信息
     *
     * @param key 取得信息的键名
     * @return 信息内容
     */
    @Override
    public Object getInfo(String key) {
        return switch (key) {
            case "mac" -> macLayer.getMacSourceAddress();
            case "ip" -> ipLayer.getIpSourceAddress();
            default -> null;
        };
    }

    /**
     * 设置网络栈中的信息：mac地址、ip地址
     *
     * @param key   mac、ip地址的键
     * @param value 被设置的值
     */
    @Override
    public void setInfo(String key, Object value) throws ParseException {
        switch (key) {
            case "mac" -> {
                if (value instanceof String) {
                    byte[] address = MoteCalculate.convertStrAddressIntoByteAddress((String) value);
                    macLayer.setMacSourceAddress(address);
                } else {
                    new ParseException("input value's type is not String").printStackTrace();
                }
            }
            case "ip" -> {
                if (value instanceof String) {
                    byte[] address = ((String) value).getBytes();
                    ipLayer.setIpSourceAddress(address);
                } else {
                    new ParseException("input value's type is not String").printStackTrace();
                }
            }
            default -> new Exception("Error Init Mote Info with key:" + key).printStackTrace();
        }
    }

    public byte[] generateMacSendingPacket(byte[] data, byte[] dstMac) throws ParseException {
        IEEE_802_11_MACLayer.Header header = IEEE_802_11_MACLayer.Header.Builder.createDataPacket(dstMac, macLayer.getMacAddress());
        return convert(data, header);
    }
}
