package com.sicnu.netsimu.core.net.mac;

import com.sicnu.netsimu.core.net.NetField;
import com.sicnu.netsimu.core.net.NetLayer;
import com.sicnu.netsimu.core.node.Node;
import com.sicnu.netsimu.exception.ParseException;
import lombok.Data;

import java.util.Arrays;
import java.util.HashMap;

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
    public static final byte[] BROAD_CAST = {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
//    public static final String BROAD_CAST = "FF:FF:FF:FF:FF:FF";
    /**
     * 网络栈中存储的MAC层地址
     */
    private byte[] selfMacAddress;

    HashMap<String, Integer> sequenceReceiveRecorder = new HashMap<>();
    HashMap<String, Integer> sequenceSendRecorder = new HashMap<>();

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
     * 检查数据包是否多余
     *
     * @param receivePacket 接受到的数据包
     * @return true 代表数据包是多余的
     */
    public boolean isDataPacketDuplicated(byte[] receivePacket) {
        boolean isRetry = Header.Builder.checkIsRetry(receivePacket);
        byte[] sc = new byte[2];
        byte[] senderAddress = new byte[6];
        Header.Builder.extractSequence(receivePacket, sc);
        Header.Builder.extractSrcAddress(receivePacket, senderAddress);
        String senderAddressString = new String(senderAddress);
        int packetSequenceNum = Header.Builder.SCtoInt(sc);
        if (!sequenceReceiveRecorder.containsKey(senderAddressString)) {
            // 如果之前未存储来自该节点的信息
            sequenceReceiveRecorder.put(senderAddressString, packetSequenceNum);
            return false;
        }
        Integer recordedSequence = sequenceReceiveRecorder.get(senderAddressString);
        if (!isRetry || recordedSequence != packetSequenceNum) {
            // 如果不是一个重传帧 必然不是一个多余帧
            sequenceReceiveRecorder.put(senderAddressString, packetSequenceNum);
            return false;
        }
        // duplicated
        return true;
    }

    /**
     * 会依据目标MAC地址，生成一个数据包头
     *
     * @param dstMac 目标MAC地址
     * @return 生成的数据包头信息
     */
    public Header createDataPacketHeader(byte[] dstMac) {
        Header header = IEEE_802_11_MACLayer.Header.Builder.createDataPacketHeader(dstMac, selfMacAddress);
        String receiverAddressString = new String(dstMac);
        Integer receiverSequence = sequenceSendRecorder.getOrDefault(receiverAddressString, 0);
        Header.Builder.SCtoByte(receiverSequence, header.SC);
        return header;
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

            public static Header createDataPacketHeader(byte[] dstMac, byte[] srcMac) {
                byte[] headerPacket = new byte[Header.HEADER_LENGTH];
                Header header = new Header(headerPacket);
                System.arraycopy(TYPE_DATA, 0, header.FC, 0, 2);
                System.arraycopy(dstMac, 0, header.Address1, 0, 6);
                System.arraycopy(srcMac, 0, header.Address2, 0, 6);
                return header;
            }

            public static Header createAckPacketHeader(byte[] dstMac, byte[] srcMac, byte[] sc) {
                byte[] headerPacket = new byte[Header.HEADER_LENGTH];
                Header header = new Header(headerPacket);
                System.arraycopy(TYPE_ACK, 0, header.FC, 0, 2);
                System.arraycopy(dstMac, 0, header.Address1, 0, 6);
                System.arraycopy(srcMac, 0, header.Address2, 0, 6);
                System.arraycopy(sc, 0, header.SC, 0, 2);
                return header;
            }

            public static boolean checkIsRetry(byte[] packet) {
                byte a = packet[1];
                a = (byte) (a >> 4 & 1);
                return a == 1;
            }

            public static void extractDstAddress(byte[] packet, byte[] target) {
                System.arraycopy(packet, 4, target, 0, 6);
            }

            public static void extractSrcAddress(byte[] packet, byte[] target) {
                System.arraycopy(packet, 10, target, 0, 6);
            }

            public static void extractSequence(byte[] packet, byte[] target) {
                System.arraycopy(packet, 22, target, 0, 2);
            }

            public static boolean isACKPacket(byte[] packet) {
                byte[] fc = new byte[2];
                System.arraycopy(packet, 0, fc, 0, 2);
                return Arrays.equals(fc, TYPE_ACK);
            }

            private static void SCtoByte(int v, byte[] scArr) {
                scArr[1] = (byte) (v & 0xff);
                scArr[0] = (byte) (v >> 8 & 0x0f);
            }

            private static int SCtoInt(byte[] scArr) {
                int ans = 0;
                int base = 1;
                for (int i = 0; i < 8; i++) {
                    if ((scArr[1] >> i & 1) == 1) {
                        ans += base;
                        base *= 2;
                    }
                }
                for (int i = 0; i < 4; i++) {
                    if ((scArr[0] >> i & 1) == 1) {
                        ans += base;
                        base *= 2;
                    }
                }
                return ans;
            }
        }
    }
}
