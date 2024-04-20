package com.example.gossipprotocol.config;

import com.example.gossipprotocol.model.GossipState;
import com.example.gossipprotocol.model.HeartBeatState;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GossipConfig {

    @Bean("myGossipState")
    public GossipState myGossipState(@Qualifier("myHeartBeatState") HeartBeatState myHeartBeatState) {
        GossipState myGossipStateObj = new GossipState();
        String myAddress = System.getenv("MY_IP").concat(":8080");
        myGossipStateObj.updateStateForAddress(myAddress, myHeartBeatState);
        return myGossipStateObj;
    }


    @Bean("myHeartBeatState")
    public HeartBeatState myHeartBeatState() {
        return new HeartBeatState();
    }

}
