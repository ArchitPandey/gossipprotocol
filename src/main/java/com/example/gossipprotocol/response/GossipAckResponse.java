package com.example.gossipprotocol.response;

import com.example.gossipprotocol.model.HeartBeatState;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class GossipAckResponse {

    Map<String, HeartBeatState> updateOnPeer;

    List<String> needUpdatedInfoNodes;

}
