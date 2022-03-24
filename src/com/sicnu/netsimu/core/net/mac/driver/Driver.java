package com.sicnu.netsimu.core.net.mac.driver;

import com.sicnu.netsimu.core.NetSimulator;
import com.sicnu.netsimu.core.net.channel.Channel;
import com.sicnu.netsimu.core.net.channel.ChannelManager;
import com.sicnu.netsimu.core.node.Node;

import java.util.List;

public abstract class Driver {

    /**
     * each driver owns a node one to one.
     */
    protected Node node;
    /**
     * the manager which could manage the channel of each node.
     */
    protected ChannelManager channelManager;
    /**
     * the channel to which the drive is connected
     */
    protected Channel channel;
    /**
     * the transmitted speed of the Driver
     */
    protected float dataBitRate;
    /**
     * 传输
     */
    protected float physicalTimeCost;

    static final float physicalBeta = (float) 0;

    /**
     * is in Idle Sleep Status?
     * it will be broken by the changeBusy() or awake()
     */
    private boolean idleSleepStatus;

    /**
     * the constructor of the Driver
     *
     * @param simulator        the citation of the simulator
     * @param node             the node which the driver belongs to
     * @param channel          the channel which the driver is linked to
     * @param dataBitRate      the dataBitRate, in per microseconds
     * @param physicalTimeCost the physical transmission time cost, in microseconds
     */
    public Driver(NetSimulator simulator, Node node, Channel channel, float dataBitRate, float physicalTimeCost) {
        this.channelManager = simulator.getChannelManager();
        this.node = node;
        this.channel = channel;
        this.dataBitRate = dataBitRate;
        this.physicalTimeCost = physicalTimeCost;
    }

    // API //

    /**
     * the monitor status, which indicates if it is "opening" or "closing".
     * if it is "opening", then the monitor could listen the channel's status.
     */
    boolean monitorStatus;

    /**
     * driver sleep a few time and then do some things.
     *
     * @param delay the delay time(nanosecond).
     */
    void sleep(int delay) {

    }

    /**
     * transmit the data through the channel
     *
     * @param data the data needed transmit
     */
    void transmit(byte[] data) {
//        float rand = NetSimulationRandom.nextFloat() * physicalBeta;
//        int costTime = (int) (physicalTimeCost * (1 + rand) + data.length / dataBitRate);
        int costTime = (int) (physicalTimeCost + data.length / dataBitRate);
        List<Channel> connectedChannels = channelManager.getConnectedChannels(node);
        for (Channel connectedChannel : connectedChannels) {
            connectedChannel.pushSignal(costTime, data);
        }
        waitWhatever(costTime);
    }

    /**
     * set the monitoring status, if the monitoring is opening(true), then the driver
     * could listen the channel's status.
     * <p>
     * if the monitoring is closing(false), the driver can't listen to it anymore.
     *
     * @param status monitoring status
     */
    void turnMonitor(boolean status) {
        monitorStatus = status;
    }

    /**
     * start listening waiting, if the channel status become idle,
     * it will trigger the function changeBusy()
     */
    void waitUntilIdle() {
        // in fact, while the signals come to the end,
        // the channel will call the driver automatically
        return;
    }

    /**
     * Used For:
     * <p>
     * start listening waiting, if the channel status become busy,
     * it will trigger the function changeIdle().
     * <p>
     * Or the clock is ended,
     * they will trigger the function awake().
     * <p>
     * Implementation:
     * <p>
     * waitInIdle() will create an macTimeoutEvent,
     * this event will finally be triggered if there's no other signals.
     * <p>
     * if there's other signals, this event will be abandoned,
     * and it will automatically trigger the changeBusy().
     */
    void waitInIdle(int time) {
        if (!idleSleepStatus) {
            idleSleepStatus = true;
            channel.waitInIdle(time);
        } else {
            new RuntimeException("can't sleep one more time while in sleeping " + node.getNodeId()).printStackTrace();
        }
    }

    private void waitWhatever(int time) {
        if (!idleSleepStatus) {
            idleSleepStatus = true;
            channel.waitWhatever(time);
        } else {
            new RuntimeException("can't sleep one more time while in sleeping(sending action)").printStackTrace();
        }
    }

    /**
     * get the channel monitor status of the driver.
     *
     * @return monitor status of the driver, is monitoring (true), or not (false).
     */
    public boolean isMonitoring() {
        return monitorStatus;
    }

    // Interface //

    /**
     * A node wants to send data, it must call this Driver's function
     *
     * @param data the data that node wants to send
     */
    public abstract void sendData(byte[] data);

    /**
     * A node use sleep() to sleep X nanoseconds,
     * and then after X nanoseconds, it will call the awake().
     * <p>
     * in code's calling, it should be called by afterIdleSleepFunctionTrigger()
     */
    public final void triggerAwake() {
        idleSleepStatus = false;
        awake();
    }

    protected abstract void awake();

    /**
     * if the driver is listening the channel (monitorStatus == true),
     * <p>
     * while the channel from Idle to Busy,
     * this function will be triggered!
     * <p>
     * in code's calling, it should be called by afterIdleSleepFunctionTrigger()
     */
    public final void triggerChangeBusy() {
        idleSleepStatus = false;
        changeBusy();
    }

    protected abstract void changeBusy();

    /**
     * if the driver is listening the channel (monitorStatus == true),
     * <p>
     * while the channel from Busy to Idle,
     * this function will be triggered!
     */
    public abstract void triggerChangeIdle();

    /**
     * receive a data packet with a channel
     *
     * @param data the data packet
     */
    public abstract void callbackReceive(byte[] data);

    public Channel getChannel() {
        return channel;
    }
}
