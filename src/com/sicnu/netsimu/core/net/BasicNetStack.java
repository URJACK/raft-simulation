package com.sicnu.netsimu.core.net;

import com.sicnu.netsimu.core.net.ip.BasicIPLayer;
import com.sicnu.netsimu.core.net.mac.BasicMACLayer;
import com.sicnu.netsimu.exception.ParseException;

import java.util.ArrayList;

/**
 * 基础网络栈：目前包含仅包含Mac层，写了一个Ip层占位。
 * NetStack 的 parse() 与 convert() 本质上就是不断调用 NetLayer的 同名函数
 *
 * @see NetLayer
 * @see BasicMACLayer
 */
public class BasicNetStack extends NetStack {
    BasicMACLayer macLayer;
    BasicIPLayer ipLayer;
    /**
     * convert 与 parse 都依赖于该变量。
     * 它是不同NetLayer传输数据的分隔符
     * <p>
     * 也正因它是通过分割符来处理层关系，而非字段长度处理层关系
     * 有一些非ASCII码（低位编码，例如 ascii码为1 ）数据无法进行传输
     */
    String SPLIT_CHAR = (char) 1 + "";

    /**
     * 基础网络栈构造函数
     *
     * @param macAddress 当前设备的MAC地址
     */
    public BasicNetStack(String macAddress) {
        macLayer = new BasicMACLayer(macAddress);
        ipLayer = new BasicIPLayer();
    }

    /**
     * 网络栈将传入的各类Header和数据转换成可以传输的字符串
     * <pre>
     *     stack.parse(new MACLayer.Header(...),new IPLayer.Header(...),data)
     * </pre>
     *
     * @param value 要传输的应用层数据内容
     * @param args  Header与传输的数据
     * @return 可传输字符串
     */
    @Override
    public String convert(String value, NetField... args) {
        try {
            String macHeader = macLayer.convert(args[0]);
            return macHeader + SPLIT_CHAR + value;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 通过网络栈检查自己是否可以接收该数据包
     *
     * @param packet 传输来的数据包
     * @return null==无法接收该数据包
     */
    @Override
    public ArrayList<NetField> parse(String packet) {
        String[] splits = packet.split(SPLIT_CHAR);
        if (splits.length != 2) {
            new ParseException("数据包字段数不匹配").printStackTrace();
            return null;
        }
        String macHeaderStr = splits[0];
        String value = splits[1];
        try {
            ArrayList<NetField> ans = new ArrayList<>(2);
            if (!macLayer.validate(macHeaderStr)) {
                return null;
            }
            NetField macHeader = macLayer.parse(macHeaderStr);
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
    public String getInfo(String key) {
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
    public void setInfo(String key, Object value) {
        switch (key) {
            case "mac" -> macLayer.setMacSourceAddress(String.valueOf(value));
            case "ip" -> ipLayer.setIpSourceAddress(String.valueOf(value));
            default -> new Exception("Error Init Mote Info with key:" + key).printStackTrace();
        }
    }

    public static void main(String[] args) {
        String a = "1234";
        Object b = a;
//        String c = (String) b;
        String c = b.toString();
        System.out.println(c);
    }
}
