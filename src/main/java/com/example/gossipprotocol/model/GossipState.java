package com.example.gossipprotocol.model;

import java.util.HashMap;
import java.util.Map;

public class GossipState {

    private Map<String, HeartBeatState> state;

    public GossipState() {
        this.state = new HashMap<String, HeartBeatState>();
    }

    public Map<String, HeartBeatState> getState() {
        return this.state;
    }

    public void updateStateForAddress(String address, HeartBeatState state) {
        this.state.put(address, state);
    }

    public HeartBeatState getHeartBeatForAddress(String address) {
        return this.state.get(address);
    }

    public void updateGossipStateFromStateMap(Map<String, HeartBeatState> latestState) {
        for (Map.Entry<String, HeartBeatState> entry: latestState.entrySet()) {
            String address = entry.getKey();
            HeartBeatState state = entry.getValue();
            this.updateStateForAddress(address, state);
        }
    }
}
