package com.sicnu.netsimu.core.net;

import java.util.ArrayList;
import java.util.List;

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
    public abstract String convert(String value, NetField... args);

    /**
     * 通过网络栈检查自己是否可以接收该数据包
     *
     * @param packet 传输来的数据包
     * @return null==无法接收该数据包
     */
    public abstract ArrayList<NetField> parse(String packet);

    /**
     * 从网络栈中取得信息
     *
     * @param key 取得信息的键名
     * @return 信息内容
     */
    public abstract String getInfo(String key);

    /**
     * 设置网络栈中的信息：mac地址、ip地址
     *
     * @param key   mac、ip地址的键
     * @param value 被设置的值
     */
    public abstract void setInfo(String key, String value);
}
