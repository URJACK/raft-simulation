package com.sicnu.netsimu.core.net.channel;

import com.sicnu.netsimu.core.NetSimulator;
import com.sicnu.netsimu.core.event.EventManager;
import com.sicnu.netsimu.core.event.MACSignalEvent;
import com.sicnu.netsimu.core.net.driver.Driver;

import java.util.*;

public class Channel {
    /**
     * the driver object connected to this channel.
     */
    Driver driver;
    /**
     * the channel will hold a citation of channelManager
     */
    ChannelManager channelManager;
    /**
     * the eventManager will control all events in the simulator.
     */
    EventManager eventManager;
    /**
     * the signals that the channel hold
     */
    ArrayList<Signal> signalsInChannel;
    /**
     * sleep signal, in the event-driven system,
     * if we want to abandon a MACSignalEvent,
     * we must use a variable to connected with it.
     * <p>
     * -----------
     * <p>
     * in waitInIdle(), sleepSignal will be assigned a value
     * <p>
     * in addSignal(), sleepSignal will be cleared if it is existed.
     * And then trigger the changeBusy().
     * <p>
     * in macEventEndingHandler(), sleepSignal will be cleared,
     * And then trigger the awake()
     *
     * @see Channel#waitInIdle(int)
     * @see Channel#pushSignal(int, byte[])
     * @see Channel#macEventEndingHandler(MACSignalEvent)
     */
    Signal sleepSignal = null;

    public Channel(NetSimulator simulator) {
        this.eventManager = simulator.getEventManager();
        this.channelManager = simulator.getChannelManager();
        this.signalsInChannel = new ArrayList<>();
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    /**
     * add a segment of data
     *
     * @param costTime the time which the transmission will cost.
     * @param data     what the node want to send
     */
    public void pushSignal(int costTime, byte[] data) {
        NetSimulator simulator = channelManager.getSimulator();
        long endTime = simulator.getTime() + costTime;
        /* why this event?
        while the event was triggered in the event queue,
        the event will notify the channel to remove related signal.
         */
        MACSignalEvent event = new MACSignalEvent(endTime, this, false);
        eventManager.pushEvent(event);
        /*
        The signal will connected to the MACEvent,
        if the signal has collided, the MACEvent will be abandoned.
        */
        Signal newSignal = new Signal(simulator.getTime(), endTime, data, event);
        insertSortWithList(this.signalsInChannel, newSignal);
        /*
        The signal status will be busy,
        now we need to report the channel's status.
         */
        if (this.signalsInChannel.size() == 1) {
            if (driver.isMonitoring()) {
                // means this is from lazy to busy
//                driver.changeBusy();
                driver.triggerChangeBusy();
                if (sleepSignal != null) {
                    /*
                     while the channel become busy from idle,
                     if sleepSignal is not null,
                     its related event must be forbidden.
                     */
                    sleepSignal.event.setAbandoned(true);
                    sleepSignal = null;
                }
            }
        } else if (this.signalsInChannel.size() == 2) {
            for (Signal signal : this.signalsInChannel) {
                signal.conflict();
            }
        } else {
            newSignal.conflict();
        }
    }

    /**
     * all the event will have the work() method
     * <p>
     * MACEvent will only do a thing: call the channel macEventEndingHandler()
     *
     * @param event macEvent Object created by addSignal()
     * @see MACSignalEvent
     */
    public void macEventEndingHandler(MACSignalEvent event) {
        if (!event.isVirtual()) {
            // this is a signal event trigger
            long triggerTime = event.getTriggerTime();
            if (triggerTime != this.signalsInChannel.get(0).end) {
                throw new RuntimeException("channel's object is not matched with event list");
            }
            // remove the first event, it must be also the trigger event.
            Signal removeSignal = this.signalsInChannel.remove(0);
            if (!removeSignal.isConflicted()) {
                // if the signal is conflicted, it won't trigger the event
                driver.callbackReceive(removeSignal.getData());
            }
            if (this.signalsInChannel.size() == 0) {
                if (driver.isMonitoring()) {
                    // this is from busy to idle
                    driver.triggerChangeIdle();
                }
            }
        } else {
            // if this is a virtual event trigger
            sleepSignal = null;
//            driver.awake();
            driver.triggerAwake();
        }
    }

    public boolean isBusy() {
        return this.signalsInChannel.size() != 0;
    }

    /**
     * insert a signal to the channel's list
     *
     * @param list   channel's list
     * @param signal the signal object
     */
    public static void insertSortWithList(ArrayList<Signal> list, Signal signal) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).end > signal.end) {
                list.add(i, signal);
                return;
            }
        }
        list.add(signal);
    }

    public void waitInIdle(int time) {
        if (isBusy() || sleepSignal != null) {
            throw new RuntimeException("a node can't sleep while the channel is busy");
        }
        NetSimulator simulator = channelManager.getSimulator();
        EventManager eventManager = simulator.getEventManager();
        long endTime = simulator.getTime() + time;
        MACSignalEvent macSleepEvent = new MACSignalEvent(endTime, this, true);
        eventManager.pushEvent(macSleepEvent);
        sleepSignal = new Signal(simulator.getTime(), endTime, null, macSleepEvent);
    }

    public void waitWhatever(int time) {
        if (sleepSignal != null) {
            throw new RuntimeException("a node can't sleep(sending operation) while the channel is busy");
        }
        NetSimulator simulator = channelManager.getSimulator();
        EventManager eventManager = simulator.getEventManager();
        long endTime = simulator.getTime() + time;
        MACSignalEvent macSleepEvent = new MACSignalEvent(endTime, this, true);
        eventManager.pushEvent(macSleepEvent);
        // sleep signal didn't connect with SignalEvent,
        // so the event will be triggered no matter what happened.
        sleepSignal = new Signal(simulator.getTime(), endTime, null, null);
        // another thing important need to know is that,
        // changeBusy() could be triggered normally during this span.
        // so if we want to implement the sending logic,
        // there must be extra codes in Driver.
    }
}
