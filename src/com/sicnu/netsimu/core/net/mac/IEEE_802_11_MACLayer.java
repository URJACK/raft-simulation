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
public class IEEE_802_11_MACLayer extends NetLayer {
    /**
     * 广播地址
     */
    public static final byte[] BROAD_CAST = {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
            (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
//    public static final String BROAD_CAST = "FF:FF:FF:FF:FF:FF";
    /**
     * 网络栈中存储的MAC层地址
     */
    private byte[] selfMacAddress;

    /**
     * 构建MAC层对象
     *
     * @param selfMacAddress MAC层地址
     */
    public IEEE_802_11_MACLayer(byte[] selfMacAddress) {
        this.selfMacAddress = selfMacAddress;
    }

    /**
     * 将一个MAC层头对象转为需要传输的字符串
     *
     * @param args MAC层头对象
     * @return "isAck,AE:FF:35:0B:95:85,AE:FF:35:0B:95:85"
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
     * @return 解析后的协议的头信息或者是应用层数据
     * @throws ParseException 解析异常
     */
    @Override
    public NetField parse(byte[] packet) throws ParseException {
        if (packet.length != Header.HEADER_LENGTH) {
            throw new ParseException("length is not matched");
        }
        return Header.Builder.parse(packet);
    }

    /**
     * 验证该层是否可以接收这个数据包
     *
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
            return Arrays.equals(head.getDes(), selfMacAddress);
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 返回节点自身的MAC层地址
     *
     * @return MAC层地址
     */
    public byte[] getMacSourceAddress() {
        return selfMacAddress;
    }

    /**
     * 设置节点自身的Mac源地址
     *
     * @param address mac源地址
     */
    public void setMacSourceAddress(byte[] address) {
        selfMacAddress = address;
    }

    public byte[] getMacAddress() {
        return selfMacAddress;
    }

    /**
     * 一个数据包包含的MAC层头信息
     */
    @Data
    public static class Header implements NetField {
        public static int HEADER_LENGTH = 30;
        byte[] FC;              //  2bits
        byte[] DID;             //  2bits
        byte[] Address1;        //  6bits
        byte[] Address2;        //  6bits
        byte[] Address3;        //  6bits
        byte[] SC;              //  2bits
        byte[] Address4;        //  6bits

        public Header(byte[] headerPacket) {
            FC = new byte[2];
            DID = new byte[2];
            Address1 = new byte[6];
            Address2 = new byte[6];
            Address3 = new byte[6];
            SC = new byte[2];
            Address4 = new byte[6];
            System.arraycopy(headerPacket, 0, FC, 0, 2);
            System.arraycopy(headerPacket, 2, DID, 0, 2);
            System.arraycopy(headerPacket, 4, Address1, 0, 6);
            System.arraycopy(headerPacket, 10, Address2, 0, 6);
            System.arraycopy(headerPacket, 16, Address3, 0, 6);
            System.arraycopy(headerPacket, 22, SC, 0, 2);
            System.arraycopy(headerPacket, 24, Address4, 0, 6);
        }

        public static boolean isACKPacket(byte[] data) {
            return false;
        }

        /**
         * {isACK(1bit),srcAddress(6bit),dstAddress(6bit)}
         *
         * @return 以Byte数组形式，返回MAC帧的数据头
         */
        @Override
        public byte[] value() {
            byte[] headerPacket = new byte[Header.HEADER_LENGTH];
            System.arraycopy(FC, 0, headerPacket, 0, 2);
            System.arraycopy(DID, 0, headerPacket, 2, 2);
            System.arraycopy(Address1, 0, headerPacket, 4, 6);
            System.arraycopy(Address2, 0, headerPacket, 10, 6);
            System.arraycopy(Address3, 0, headerPacket, 16, 6);
            System.arraycopy(SC, 0, headerPacket, 22, 2);
            System.arraycopy(Address4, 0, headerPacket, 24, 6);
            return headerPacket;
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

        public byte[] getDes() {
            return Address1;
        }

        public static boolean isBroadCastPacket(byte[] packet) {
            byte[] desAddress = new byte[6];
            System.arraycopy(packet, 4, desAddress, 0, 6);
            return Arrays.equals(desAddress, BROAD_CAST);
        }

        public static class Builder {

            // b'00100000'  b'00000000'
            static final byte[] TYPE_DATA = {0x20, 0x00};
            // b'00011101'  b'00000000'
            static final byte[] TYPE_ACK = {0x1D, 0x00};

            /**
             * @param layerPacket [Header(30 bits) + FrameBody(0~2312 bits) + FCS(4 bits) ]
             * @return 返回创建的 Header 对象
             */
            static Header parse(byte[] layerPacket) {
                byte[] headerPacket = new byte[Header.HEADER_LENGTH];
                System.arraycopy(layerPacket, 0, headerPacket, 0, Header.HEADER_LENGTH);
                return new Header(headerPacket);
            }

            public static Header createDataPacket(byte[] dstMac, byte[] srcMac) {
                byte[] headerPacket = new byte[Header.HEADER_LENGTH];
                Header header = new Header(headerPacket);
                System.arraycopy(TYPE_DATA, 0, header.FC, 0, 2);
                System.arraycopy(dstMac, 0, header.Address1, 0, 6);
                System.arraycopy(srcMac, 0, header.Address2, 0, 6);
                return header;
            }

            public static Header createAckPacket(byte[] dstMac, byte[] srcMac) {
                byte[] headerPacket = new byte[Header.HEADER_LENGTH];
                Header header = new Header(headerPacket);
                System.arraycopy(TYPE_ACK, 0, header.FC, 0, 2);
                System.arraycopy(dstMac, 0, header.Address1, 0, 6);
                System.arraycopy(srcMac, 0, header.Address2, 0, 6);
                return header;
            }

            public static void extractDstAddress(byte[] packet, byte[] target) {
                System.arraycopy(packet, 4, target, 0, 6);
            }

            public static void extractSrcAddress(byte[] packet, byte[] target) {
                System.arraycopy(packet, 10, target, 0, 6);
            }

            public static byte[] buildACKPacket(byte[] packet) {
                byte[] senderPacket = new byte[6];
                byte[] selfPacket = new byte[6];
                extractSrcAddress(packet, senderPacket);
                extractDstAddress(packet, selfPacket);
                Header data = createAckPacket(senderPacket, selfPacket);
                return data.value();
            }
        }
    }
}
