package com.sicnu.raft.role.rpc;

/**
 * RPC 转换接口
 * 所有的RPC要想正常的与字符串进行转换
 * 就必须要继承该接口
 */
public interface RPCConvert {
    /**
     * 将自身的数据转为 压缩字符串 ，用于输出传输
     */
    String convert();

    /**
     * 解析 压缩字符串 ，并将数据放入自己的成员变量中
     *
     * @param str 压缩字符串
     */
    void parse(String str);
}
