package com.example.gossipprotocol.api;

import com.example.gossipprotocol.model.GossipState;
import com.example.gossipprotocol.request.GossipAck2Request;
import com.example.gossipprotocol.request.GossipSynRequest;
import com.example.gossipprotocol.response.GossipAckResponse;
import com.example.gossipprotocol.service.GossipService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/gossip")
public class GossipApi {

    GossipService gossipService;

    public GossipApi(GossipService gossipService) {
        this.gossipService = gossipService;
    }

    @GetMapping("/mystate")
    public ResponseEntity<GossipState> myGossipState() {
        return ResponseEntity.ok(this.gossipService.myState());
    }

    @PostMapping(path = "/syn")
    public ResponseEntity<GossipAckResponse> gossipSyn(@RequestBody GossipSynRequest gossipSynRequest) {
        return ResponseEntity.ok(this.gossipService.processGossipSynRequest(gossipSynRequest));
    }

    @PostMapping(path = "/ack2")
    public ResponseEntity<String> gossipAck2(@RequestBody GossipAck2Request gossipAck2Request) {
        this.gossipService.updateMyGossipState(gossipAck2Request.getStatesMap());
        return ResponseEntity.ok("ok");
    }
}
