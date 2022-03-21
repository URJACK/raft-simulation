package com.sicnu.netsimu.core.net.driver;

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
    private final float bitSpeed;
    /**
     * 传输
     */
    private final float transmitBaseCost;

    /**
     * is in Idle Sleep Status?
     * it will be broken by the changeBusy() or awake()
     */
    private boolean idleSleepStatus;

    public Driver(NetSimulator simulator, Node node, Channel channel, float bitSpeed, float transmitBaseCost) {
        this.channelManager = simulator.getChannelManager();
        this.node = node;
        this.channel = channel;
        this.bitSpeed = bitSpeed;
        this.transmitBaseCost = transmitBaseCost;
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
        int costTime = (int) (transmitBaseCost + data.length / bitSpeed);
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
            new RuntimeException("can't sleep one more time while in sleeping").printStackTrace();
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
     * this is the function trigger,
     * why should we use this one ? why don't we just use awake() or changeBusy()?
     * <p>
     * these two functions both need to clear a flag variable called "idleSleepStatus".
     * we don't want to make user know too many details about the Driver.
     *
     * @param isTriggerAwake if this is true, it will trigger awake(), or it will trigger changeBusy()
     * @see Driver#triggerAwake()
     * @see Driver#triggerChangeBusy()
     */
    public final void afterIdleSleepFunctionTrigger(boolean isTriggerAwake) {
        idleSleepStatus = false;
        if (isTriggerAwake) {
            triggerAwake();
        } else {
            triggerChangeBusy();
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
     *
     * @see Driver#afterIdleSleepFunctionTrigger(boolean)
     */
    protected abstract void triggerAwake();

    /**
     * if the driver is listening the channel (monitorStatus == true),
     * <p>
     * while the channel from Idle to Busy,
     * this function will be triggered!
     * <p>
     * in code's calling, it should be called by afterIdleSleepFunctionTrigger()
     *
     * @see Driver#afterIdleSleepFunctionTrigger(boolean)
     */
    protected abstract void triggerChangeBusy();

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
