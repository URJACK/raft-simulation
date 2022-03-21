package com.sicnu.netsimu.core.net.channel;

import com.sicnu.netsimu.core.NetSimulator;
import com.sicnu.netsimu.core.net.TransmissionManager;
import com.sicnu.netsimu.core.node.Node;

import java.util.LinkedList;
import java.util.List;

public class ChannelManager {
    NetSimulator simulator;
    TransmissionManager transmissionManager;

    public ChannelManager(NetSimulator netSimulator) {
        this.simulator = netSimulator;
        this.transmissionManager = netSimulator.getTransmissionManager();
    }

    public List<Channel> getConnectedChannels(Node node) {
        List<TransmissionManager.Neighbor> neighbors = transmissionManager.getNeighbors(node);
        List<Channel> ans = new LinkedList<>();
        for (TransmissionManager.Neighbor neighbor : neighbors) {
            ans.add(neighbor.getNode().getDriver().getChannel());
        }
        return ans;
    }

    public NetSimulator getSimulator() {
        return simulator;
    }
}
