package com.sicnu.netsimu.raft;

import com.sicnu.netsimu.core.utils.NetSimulationRandom;
import com.sicnu.netsimu.raft.exception.ParameterException;

/**
 * Raft算法运行过程中的相关工具函数
 */
public class RaftUtils {

    /**
     * 转换得到发送的Ip地址
     *
     * @param ipPrefix IP地址网络前缀：RaftMote.IP_PREFIX
     * @param moteId   Mote的Id，从[1,NODE_NUM]
     * @return
     */
    public static String convertIpAddressWithMoteId(String ipPrefix, int moteId) {
        return ipPrefix + moteId;
    }

    /**
     * 获取val的浮动值
     *
     * @param val  输入值 eg:500
     * @param beta 浮动比例 eg:0.1
     * @return 返回输入值的浮动值 eg:[500*0.9, 500*1.1]
     */
    public static long floatValue(long val, float beta) {
        if (beta >= 1) {
            //非法的beta
            new ParameterException("floatValue's beta can't be bigger than 1").printStackTrace();
            return 0;
        }
        double offset = NetSimulationRandom.nextFloat() * val * beta * 2;
        return (long) (val * (1 - beta) + offset);
    }

    /**
     * 取得形如 "3,3,4235,6" 的第一个数字
     *
     * @param data 字符串
     * @return 返回字符串的第一个数字，如果非法，则会返回0
     */
    public static int getFirstValFromString(String data) {
        for (int i = 0; i < data.length(); i++) {
            if (data.charAt(i) == ',') {
                return Integer.parseInt(data.substring(0, i));
            }
        }
        return -1;
    }
}
