package com.example.gossipprotocol.request;

import com.example.gossipprotocol.model.HeartBeatState;
import lombok.Data;

import java.util.Map;

@Data
public class GossipAck2Request {

    Map<String, HeartBeatState> statesMap;
}
