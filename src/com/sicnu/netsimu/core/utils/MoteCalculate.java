package com.sicnu.netsimu.core.utils;

import com.sicnu.netsimu.core.node.Node;
import com.sicnu.netsimu.exception.ParseException;

public class MoteCalculate {
    public static float eulaDistance(Node a, Node b) {
        float ySpan = Math.abs(a.getY() - b.getY());
        float xSpan = Math.abs(a.getX() - b.getX());
        return (float) Math.sqrt(ySpan * ySpan + xSpan * xSpan);
    }

    /**
     * 转换得到发送的Ip地址
     *
     * @param macPrefix IP地址网络前缀：RaftMote.MAC_PREFIX
     * @param moteId    Mote的Id，从[1,NODE_NUM]
     * @return
     */
    public static byte[] convertMACAddressWithMoteId(byte[] macPrefix, int moteId) throws ParseException {
        byte[] macAddress = new byte[6];
        String hexMoteId = intToHexMAC(moteId);
        if (macPrefix.length != 5) {
            throw new ParseException("MacPrefix.length != 5.");
        }
        System.arraycopy(macPrefix, 0, macAddress, 0, 5);
        macAddress[5] = (byte) moteId;
        return macAddress;
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
            sb.append(b[n % 16]);
            n = n / 16;
        }
        a = sb.reverse().toString();
        if (a.length() == 1) {
            return "0" + a;
        }
        return a;
    }

    /**
     * 我们这里默认mac地址是 6Byte
     *
     * @param macAddressString 形如"FF:FF:FF:FF:FF:FF"的MAC地址字符串
     * @return MAC地址的byte对象数组
     */
    public static byte[] convertStrAddressIntoByteAddress(String macAddressString) throws ParseException {
        byte[] data = new byte[6];
        String[] arr = macAddressString.split(":");
        if (arr.length != 6) {
            throw new ParseException("MAC地址不是6个字节");
        }
        for (int i = 0; i < 6; i++) {
            if (arr[i].length() != 2) {
                throw new ParseException("有单个Byte超过2个字符串");
            }
            data[i] = convertByteValueFromCharacter(arr[i]);
        }
        return data;
    }

    /**
     * 转换一个2个字节的代表着byte信息的字符串： "EE" -> 0xEE
     * <p>
     * 本方法不会对 s 的输入值做任何的校验，校验请在之前的步骤完成
     *
     * @param s 需要被转换的字符串
     * @return 被转换成为的Byte值
     */
    private static byte convertByteValueFromCharacter(String s) {
        //计算第1个元素值
        char c = s.charAt(0);
        int val = 0;
        //计算十位
        if (c <= '9') {
            // 0 ~ 9
            val += c - '0';
        } else {
            // A ~ F
            val += c - 'A' + 10;
        }
        val *= 16;
        //计算个位
        c = s.charAt(1);
        if (c <= '9') {
            // 0 ~ 9
            val += c - '0';
        } else {
            // A ~ F
            val += c - 'A' + 10;
        }
        return (byte) val;
    }
}
