# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

**Build:**
```bash
./mvnw clean package
```

**Run locally:**
```bash
java -jar target/gossipprotocol-0.0.3-SNAPSHOT.jar
```

**Build Docker image:**
```bash
docker build -t archit05/gossipprotocol:0.0.3 .
```

**Run tests:**
```bash
./mvnw test
```

## Kubernetes Deployment

Deploy in this order:
```bash
kubectl apply -f seed-pod-spec.yml     # Seed node pod
kubectl apply -f seed-svc.yml          # Seed node service
# Update SEED_ADDRESS in nonseed-deployment.yml with seed pod IP first
kubectl apply -f nonseed-deployment.yml  # 3 non-seed replicas
```

Environment variables required per pod:
- `MY_IP`: Pod's own IP (injected via Kubernetes downward API)
- `IS_SEED`: `Y` for seed node, `N` for others
- `SEED_ADDRESS`: `<seed-pod-ip>:8080`

## Architecture

This is a Spring Boot implementation of the Gossip Protocol (as used by Apache Cassandra for node discovery and failure detection). Nodes exchange state via HTTP in a 3-way handshake: **SYN → ACK → ACK2**.

### Gossip Handshake Flow
1. Every 60s, a node initiates gossip by sending `POST /gossip/syn` to a randomly chosen peer (falls back to seed node if no peers known)
2. The receiving node compares states and replies with an `ACK` containing: states it knows that the sender doesn't (or has stale), plus a list of addresses where the sender has newer data
3. The initiator sends `POST /gossip/ack2` with the fresh states the peer requested

### State Model
- **`HeartBeatState`**: Per-node state with `generation` (epoch timestamp, resets daily) and an atomic `heartbeat` counter
- **`GossipState`**: A shared map of `address → HeartBeatState` for all known nodes; updated in-place via gossip exchanges
- **`HeartBeatService`**: Updates the local node's own heartbeat every 40s

### Key Classes
- `GossipService` — all gossip logic: scheduling, peer selection, SYN/ACK/ACK2 processing
- `GossipApi` — REST endpoints: `GET /gossip/mystate`, `POST /gossip/syn`, `POST /gossip/ack2`
- `GossipConfig` — defines singleton Spring beans for `myGossipState` and `myHeartBeatState`

### Configuration (`application.yml`)
```yaml
heartbeat.update-frequency: 40000ms   # How often local heartbeat increments
gossip.frequency: 60000ms             # How often gossip rounds are initiated
```
