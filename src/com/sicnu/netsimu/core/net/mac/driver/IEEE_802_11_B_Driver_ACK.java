package com.sicnu.netsimu.core.net.mac.driver;

import com.sicnu.netsimu.core.NetSimulator;
import com.sicnu.netsimu.core.event.TransmissionEvent;
import com.sicnu.netsimu.core.net.channel.Channel;
import com.sicnu.netsimu.core.node.Node;
import com.sicnu.netsimu.core.utils.NetSimulationRandom;

import java.util.Deque;
import java.util.LinkedList;

public class IEEE_802_11_B_Driver_ACK extends Driver {
    private static int S_IFS = 10;          // us
    private static int P_IFS = 30;          // us
    private static int D_IFS = 50;          // us
    private static int SLOT_TIME = 20;      // us
    DriverRole role = DriverRole.FREE;
    SendingLogic sendingLogic;
    BackoffLogic backoffLogic;

    /**
     * the constructor of the Driver
     *
     * @param simulator        the citation of the simulator
     * @param node             the node which the driver belongs to
     * @param channel          the channel which the driver is linked to
     * @param dataBitRate      the dataBitRate, in per microseconds
     * @param physicalTimeCost the physical transmission time cost, in microseconds
     */
    public IEEE_802_11_B_Driver_ACK(NetSimulator simulator, Node node, Channel channel, float dataBitRate, float physicalTimeCost) {
        super(simulator, node, channel, dataBitRate, physicalTimeCost);
        sendingLogic = new SendingLogic();
        backoffLogic = new BackoffLogic();
        // turn on the switch, to allow the changeBusy & changeIdle worked.
        turnMonitor(true);
    }

    /**
     * A node wants to send data, it must call this Driver's function
     *
     * @param data the data that node wants to send
     */
    @Override
    public void sendData(byte[] data) {
        // this will not trigger the:  role = DriverRole.CACHING;
        // because we want to realize the buffer mechanism.
        sendingLogic.pushData(data);
        switch (role) {
            case FREE ->
                    // if there's only one packet waiting to send.
                    actionTrySend();
            default -> {
            }
        }
    }

    /**
     * this function will be called in 2 situations:
     * 1` sendData(byte[]): ready to send another packet.
     * 2` awake() -> case CACHING : after transmit a packet no matter it's success or failed.
     *
     * @see IEEE_802_11_B_Driver_ACK#sendData(byte[])
     * @see IEEE_802_11_B_Driver_ACK#triggerAwake()
     */
    private void actionTrySend() {
        // clear the backoff related variables
        backoffLogic.clear();
//        role = DriverRole.CACHING;
        if (channel.isBusy()) {
            role = DriverRole.WAIT_IDLE;
            backoffLogic.detectBusyAndBackOff();
            waitUntilIdle();
            // it will trigger changeIdle()
        } else {
            role = DriverRole.WAIT_IFS;
            waitInIdle(D_IFS);
            // it will trigger awake() or changeBusy()
        }
    }

    /**
     * while in (WAIT_IFS \ WAIT_BACKOFF),
     * <p>
     * it could use this function.
     * <p>
     * this function will call transmit, and create an event automatically
     * and get into a status CACHING
     */
    private void actionSending() {
        role = DriverRole.CACHING;
        byte[] packet = sendingLogic.peek();
        sendingLogic.activateSending();
        // sending the packet
        transmit(packet);
    }

    /**
     * while in (WAIT_IFS),
     * <p>
     * it could use this function.
     * <p>
     * this function will get into a status CACHING
     */
    private void actionFailed() {
        role = DriverRole.CACHING;
        /*
         it won't trigger this status,
         so don't need sendingLogic.clearActivateSending();
         */
        sendingResultBack(false);
    }

    private void sendingResultBack(boolean result) {
        node.netSendResult(sendingLogic.peek(), result);
        sendingLogic.poll();
        if (sendingLogic.getBufferSize() == 0) {
            role = DriverRole.FREE;
        } else {
            actionTrySend();
        }
    }

    @Override
    protected void awake() {
        switch (role) {
            case WAIT_IFS -> {
                // this will trigger the sending
                if (backoffLogic.isInBackOff()) {
                    if (backoffLogic.overFailed()) {
                        actionFailed();
                    } else {
                        role = DriverRole.WAIT_BACKOFF;
                        backoffLogic.triggerTimeRecord();
                        waitInIdle(backoffLogic.getLeftBackoffLeftTime());
                    }
                } else {
                    // role = DriverRole.ACTION_SENDING;
                    // role from WAIT_IFS -> CACHING
                    actionSending();
                }
            }
            case WAIT_BACKOFF -> {
                backoffLogic.triggerTimeRefresh();
                if (backoffLogic.getLeftBackoffLeftTime() != 0) {
                    new RuntimeException("while the sending, the left Time is not equal to zero.").printStackTrace();
                }
                actionSending();
            }
            case CACHING -> {
                // actionSending() will use sendingLogic.activateSending()
                sendingLogic.clearActivateSending();
                sendingResultBack(true);
            }
        }
    }

    @Override
    protected void changeBusy() {
        // if in the following status, shall we be in the ACTION_SENDING ?
        if (sendingLogic.isSending()) {
            // if the driver is sending, it will not trigger anything
            return;
        }
        switch (role) {
            case WAIT_IFS -> {
                // ready to send a packet
                role = DriverRole.WAIT_IDLE;
                // if it has already in the backoff status,
                // while it detected the busy channel, it just needed to go back to WAIT_IDLE again.
                if (!backoffLogic.isInBackOff()) {
                    // if it is not in backoff status,
                    // it will trigger the backoff operation and set leftTime.
                    backoffLogic.detectBusyAndBackOff();
                }
            }
            case WAIT_BACKOFF -> {
                role = DriverRole.WAIT_IDLE;
                // while in the action backoff,
                // it will stop the backoff time counting.
                backoffLogic.triggerTimeRefresh();
            }
        }
    }

    /**
     * if the driver is listening the channel (monitorStatus == true),
     * <p>
     * while the channel from Busy to Idle,
     * this function will be triggered!
     */
    @Override
    public void triggerChangeIdle() {
        switch (role) {
            /**
             * @see IEEE_802_11_B_Driver_ACK#actionSending()
             * @see IEEE_802_11_B_Driver_ACK#actionFailed()
             * @see IEEE_802_11_B_Driver_ACK.DriverRole#WAIT_BACKOFF
             */
            case WAIT_IDLE -> {
                role = DriverRole.WAIT_IFS;
                // wait for a D_IFS time.
                waitInIdle(D_IFS);
                // if it triggered awake() -> , WAIT_BACKOFF, actionSending(), actionFailed()
            }
        }
    }

    /**
     * receive a data packet with a channel
     *
     * @param data the data packet
     */
    @Override
    public void callbackReceive(byte[] data) {
        // 链路层驱动，读取到数据包，创建一个传输事件，返回给上层进行调用
        NetSimulator simulator = channelManager.getSimulator();
        TransmissionEvent event = new TransmissionEvent(simulator.getTime(), this.node, data);
        simulator.getEventManager().pushEvent(event);
    }

    public enum DriverRole {
        FREE, CACHING, WAIT_IDLE, WAIT_IFS, WAIT_BACKOFF, ACTION_FAILED, ACTION_SENDING
    }

    private class BackoffLogic {
        public static final int OVER_FAIL_TIMES = 7;

        int backoffLeft;
        int backoffCount;
        long recordTime;

        public BackoffLogic() {
            clear();
        }

        private void clear() {
            backoffLeft = 0;
            backoffCount = 0;
        }

        /**
         * call this function while in (CACHING \ WAIT_IFS);
         */
        public void detectBusyAndBackOff() {
            backoffLeft = calculateTime(backoffCount);
            backoffCount++;
//            System.out.println("DEBUG BACKOFF count:" + backoffCount + " left:" + backoffLeft + " nodeId:" + node.getNodeId());
        }

        /**
         * calculate how much time the node need to backoff
         * time from 31 -> 1023
         * 2^5          ->  2^10
         * t = 0        ->  t = 5
         *
         * @param backoffCount have already done how many times backoff
         * @return the time the node need to backoff
         */
        private int calculateTime(int backoffCount) {
            int CW = (int) Math.pow(2, Math.min(backoffCount, 5) + 5) - 1;
            return (int) (CW * NetSimulationRandom.nextFloat() * SLOT_TIME);
        }

        public boolean isInBackOff() {
            return backoffLeft > 0;
        }

        public int getLeftBackoffLeftTime() {
            return backoffLeft;
        }

        public void triggerTimeRecord() {
            recordTime = node.getSimulator().getTime();
        }

        public void triggerTimeRefresh() {
            backoffLeft -= node.getSimulator().getTime() - recordTime;
        }

        public boolean overFailed() {
            return backoffCount > OVER_FAIL_TIMES;
        }
    }

    private static class SendingLogic {

        /**
         * the sending buffer,
         * it will send the first element during the process,
         * until sending success or sending failure.
         */
        Deque<byte[]> sendingBuffer;
        private boolean sending;

        public SendingLogic() {
            sendingBuffer = new LinkedList<>();
        }

        public void pushData(byte[] data) {
            sendingBuffer.addLast(data);
        }

        public byte[] peek() {
            return sendingBuffer.peekFirst();
        }

        public byte[] poll() {
            return sendingBuffer.pollFirst();
        }

        public int getBufferSize() {
            return sendingBuffer.size();
        }

        public void activateSending() {
            sending = true;
        }

        public void clearActivateSending() {
            sending = false;
        }

        public boolean isSending() {
            return sending;
        }
    }
}
