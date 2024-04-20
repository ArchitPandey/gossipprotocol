package com.example.gossipprotocol.service;

import com.example.gossipprotocol.request.GossipAck2Request;
import com.example.gossipprotocol.request.GossipSynRequest;
import com.example.gossipprotocol.model.GossipState;
import com.example.gossipprotocol.model.HeartBeatState;
import com.example.gossipprotocol.response.GossipAckResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GossipService {

    private String seedAddress;

    private boolean isSeedNode;

    private String myAddress;

    private GossipState myGossipState;

    private RestTemplate restTemplate;

    public GossipService(
            @Qualifier("myGossipState") GossipState myGossipState,
            RestTemplate restTemplate
    ) {
        this.seedAddress = System.getenv("SEED_ADDRESS");
        this.isSeedNode = System.getenv("IS_SEED").equalsIgnoreCase("Y");
        this.myAddress = System.getenv("MY_IP").concat(":8080");
        this.myGossipState = myGossipState;
        this.restTemplate = restTemplate;
    }

    public GossipAckResponse processGossipSynRequest(GossipSynRequest gossipSynRequest) {
        Map<String, HeartBeatState> myState = this.myGossipState.getState();

        Map<String, HeartBeatState> peerState = gossipSynRequest.getStatesMap();

        Map<String, HeartBeatState> updateOnPeer = filterLatestStateOnMine(myState, peerState);

        List<String> needInfoOnPeers = filterPeersWithStaleStateOnMine(myState, peerState);

        return GossipAckResponse
                .builder()
                .updateOnPeer(updateOnPeer)
                .needUpdatedInfoNodes(needInfoOnPeers)
                .build();
    }

    public void updateMyGossipState(Map<String, HeartBeatState> latestState) {
        this.myGossipState.updateGossipStateFromStateMap(latestState);
    }

    public GossipState myState() {
        return this.myGossipState;
    }

    @Scheduled(fixedDelayString = "${gossip.frequency}")
    public void scheduledGossip() {
        String gossipPeerAddress = selectPeerForGossip();
        if(gossipPeerAddress != null) {
            doGossipRoundWithPeer(gossipPeerAddress);
        }
    }

    private String selectPeerForGossip() {
        log.info("select peer for gossip: start");

        List<String> peerAddresses = this.myGossipState
                .getState()
                .keySet()
                .stream()
                .filter(address -> !address.equalsIgnoreCase(this.myAddress))
                .collect(Collectors.toList());

        if (peerAddresses.isEmpty()) {
            log.info("peerAddresses is empty, checking isSeedNode");
            if (this.isSeedNode) {
                log.info("no peer found: this is seed node with no other peer addresses");
                return null;
            } else {
                log.info("selected seed node as peer {}", this.seedAddress);
                return this.seedAddress;
            }
        }

        Random random = new Random();
        int randomPeerAddressIndex = random.nextInt(peerAddresses.size());

        log.info("select peer for gossip end: peer selected ", peerAddresses.get(randomPeerAddressIndex));
        return peerAddresses.get(randomPeerAddressIndex);
    }

    private void doGossipRoundWithPeer(String peerAddress) {
        try {
            log.info("do gossip with peer: start: executing syn request");

            String peerSynResource = buildResourceUri(peerAddress, "/gossip/syn");

            GossipSynRequest synRequest = new GossipSynRequest();
            synRequest.setFromIp(this.myAddress);
            synRequest.setStatesMap(this.myGossipState.getState());

            HttpEntity<GossipSynRequest> synRequestEntity = new HttpEntity<>(synRequest);

            ResponseEntity<GossipAckResponse> ackResponse = this.restTemplate.exchange(peerSynResource, HttpMethod.POST, synRequestEntity, GossipAckResponse.class);

            if (ackResponse.getStatusCode().is2xxSuccessful()) {
                GossipAckResponse ackResponseBody = ackResponse.getBody();
                updateMyGossipState(ackResponseBody.getUpdateOnPeer());
                Map<String, HeartBeatState> latestStateForPeers = this.getLatestStateForPeers(ackResponseBody.getNeedUpdatedInfoNodes());

                log.info("do gossip with peer: execute ack2 request");

                GossipAck2Request ack2Request = new GossipAck2Request();
                ack2Request.setStatesMap(latestStateForPeers);
                doAck2WithPeer(peerAddress, ack2Request);

                log.info("do gossip with peer: completed");
            } else {
                log.error("gossip syn request error with peer {}, statusCode {}", peerSynResource, ackResponse.getStatusCode().value());
                throw new RuntimeException("Gossip Syn Request Error");
            }
        } catch (Exception e) {
            log.error("gossip error ", e);
        }
    }

    public void doAck2WithPeer(String peerAddress, GossipAck2Request ack2Request) {
        String peerAck2Resource = buildResourceUri(peerAddress, "/gossip/ack2");

        HttpEntity<GossipAck2Request> ack2RequestEntity = new HttpEntity<>(ack2Request);

        ResponseEntity<String> ack2Response = this.restTemplate.exchange(peerAck2Resource, HttpMethod.POST, ack2RequestEntity, String.class);

        if (!ack2Response.getStatusCode().is2xxSuccessful()) {
            log.error("ack2 error with peer {}, status {}", peerAck2Resource, ack2Response.getStatusCode().value());
            throw new RuntimeException("ack2 error with peer ".concat(peerAck2Resource));
        }
    }

    private Map<String, HeartBeatState> getLatestStateForPeers(List<String> peers) {
        return peers.stream().collect(
                () -> new HashMap<String, HeartBeatState>(),
                (map, address) -> map.put(address, this.myGossipState.getHeartBeatForAddress(address)),
                (map1, map2) -> map1.putAll(map2)
                );
    }

    private Map<String, HeartBeatState> filterLatestStateOnMine(Map<String, HeartBeatState> myState, Map<String, HeartBeatState> peerState) {
        Map<String, HeartBeatState> latestStateOnMine = new HashMap<>();

        for (Map.Entry<String, HeartBeatState> entry: myState.entrySet()) {
            String address = entry.getKey();
            HeartBeatState stateOnMine = entry.getValue();

            if (!peerState.containsKey(address)) {
                latestStateOnMine.put(address, stateOnMine);
            } else {
                HeartBeatState peerHeartBeat = peerState.get(address);
                if (stateOnMine.compareTo(peerHeartBeat) > 0) {
                    latestStateOnMine.put(address, stateOnMine);
                }
            }
        }

        return latestStateOnMine;
    }

    private List<String> filterPeersWithStaleStateOnMine(Map<String, HeartBeatState> myState, Map<String, HeartBeatState> peerState) {
        List<String> needInfoOnPeers = new ArrayList<>();

        for(Map.Entry<String, HeartBeatState> entry: peerState.entrySet()) {
            String peerAddress = entry.getKey();
            HeartBeatState peerHeartBeat = entry.getValue();

            if (!myState.containsKey(peerAddress)) {
                needInfoOnPeers.add(peerAddress);
            } else {
                HeartBeatState myStateForPeer = myState.get(peerAddress);
                if (myStateForPeer.compareTo(peerHeartBeat) < 0) {
                    needInfoOnPeers.add(peerAddress);
                }
            }
        }

        return needInfoOnPeers;
    }

    private String buildResourceUri(String address, String resourcePath) {
        return new StringBuilder()
                .append("http://")
                .append(address)
                .append(resourcePath)
                .toString();
    }

}
