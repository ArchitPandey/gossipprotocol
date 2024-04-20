package com.example.gossipprotocol.service;

import com.example.gossipprotocol.model.HeartBeatState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class HeartBeatService {

    private HeartBeatState myHeartBeatState;

    public HeartBeatService(
            @Qualifier("myHeartBeatState") HeartBeatState myHeartBeatState) {
        this.myHeartBeatState = myHeartBeatState;
    }

    @Scheduled(fixedDelayString = "${heartbeat.update-frequency}")
    public void updateMyHeartBeat() {
        this.myHeartBeatState.increment();
    }

}
