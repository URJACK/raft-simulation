package com.sicnu.netsimu.core.utils;

import com.sicnu.netsimu.core.mote.Mote;

public class MoteCalculate {
    public static float eulaDistance(Mote a, Mote b) {
        float ySpan = Math.abs(a.getY() - b.getY());
        float xSpan = Math.abs(a.getX() - b.getX());
        return (float) Math.sqrt(ySpan * ySpan + xSpan * xSpan);
    }

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
     * 转换得到发送的Ip地址
     *
     * @param macPrefix IP地址网络前缀：RaftMote.MAC_PREFIX
     * @param moteId    Mote的Id，从[1,NODE_NUM]
     * @return
     */
    public static String convertMACAddressWithMoteId(String macPrefix, int moteId) {
        String hexMoteId = intToHexMAC(moteId);
        return macPrefix + hexMoteId;
    }


    /**
     * 0 -- "00"
     * 8 -- "08"
     * 215 -- "D7"
     * 255 -- "FF"
     *
     * @param n 需要被转换的数字
     * @return
     */
    public static String intToHexMAC(int n) {
        if (n == 0) {
            return "00";
        }
        StringBuilder sb = new StringBuilder(8);
        String a;
        char[] b = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        while (n != 0) {
            sb = sb.append(b[n % 16]);
            n = n / 16;
        }
        a = sb.reverse().toString();
        if (a.length() == 1) {
            return "0" + a;
        }
        return a;
    }
}
