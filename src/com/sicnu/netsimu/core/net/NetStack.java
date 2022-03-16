package com.sicnu.netsimu.core.net;

import com.sicnu.netsimu.core.net.mac.BasicMACLayer;
import com.sicnu.netsimu.exception.ParseException;

import java.util.ArrayList;

public abstract class NetStack {

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
    public abstract byte[] convert(byte[] value, NetField... args);

    /**
     * 通过网络栈检查自己是否可以接收该数据包
     *
     * @param packet 传输来的数据包
     * @return null==无法接收该数据包
     */
    public abstract ArrayList<NetField> parse(byte[] packet);

    /**
     * 外部调用接口。从网络栈中取得信息
     * <pre>
     * BasicMACLayer.Header header =
     * new BasicMACLayer.Header(netStack.getInfo("mac"), dstMac);
     * </pre>
     * netStack应该做出类似如下的实现：
     * <pre>
     *  getInfo(String key) {
     *      return switch (key) {
     *          case "mac" -> macLayer.getMacSourceAddress();
     *          case "ip" -> ipLayer.getIpSourceAddress();
     *          default -> null;
     *      };
     *  }
     * </pre>
     *
     * @param key 取得信息的键名
     * @return 信息内容
     */
    public abstract Object getInfo(String key);

    /**
     * 设置网络栈中的信息：mac地址、ip地址
     *
     * @param key   mac、ip地址的键
     * @param value 被设置的值
     */
    public abstract void setInfo(String key, Object value) throws ParseException;

    public byte[] macSendingPacket(byte[] data, byte[] dstMac) throws ParseException {
        BasicMACLayer.Header header = null;
        header = new BasicMACLayer.Header((byte[]) getInfo("mac"), dstMac);
        return convert(data, header);
    }
}
