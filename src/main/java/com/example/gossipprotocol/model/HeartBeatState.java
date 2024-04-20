package com.example.gossipprotocol.model;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.concurrent.atomic.AtomicInteger;

public class HeartBeatState implements Comparable<HeartBeatState> {

    private long generation;

    private AtomicInteger heartBeat;

    public HeartBeatState() {
        this.generation = LocalDate
                .now()
                .atStartOfDay()
                .atZone(ZoneId.of("UTC"))
                .toInstant()
                .getEpochSecond();
        this.heartBeat = new AtomicInteger(0);
    }

    public void increment() {
        this.heartBeat.incrementAndGet();
    }

    public int getHeartBeat() {
        return this.heartBeat.intValue();
    }

    public long getGeneration() {
        return this.generation;
    }

    @Override
    public int compareTo(HeartBeatState s) {
        if(this.getGeneration() == s.getGeneration()) {
            return this.getHeartBeat() - s.getHeartBeat();
        } else {
            long diff = (this.getGeneration() - s.getGeneration());
            if (diff > 0) {
                return 1;
            } else if (diff < 0) {
                return -1;
            } else {
                return 0;
            }
        }
    }
}
