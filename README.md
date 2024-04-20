
# Gossip Protocol Implementation

Gossip Protocol is used by Cassandra to discover other nodes in the cluster and also detect node failures.

This is a very limited implementation of Gossip Protocol. This implementation uses Kubernetes Pods to simulate Cassandra nodes. The nodes communicate (gossip) with each other over http.






## Installation

- Run the seed-pod-spec.yml to create the seed pod first. Node down the ip address of the seed pod

- Run seed-svc.yml to create service that exposes seed pod. Note down port of seed-service.

- Do a request from browser : http://localhost:<port>/gossip/mystate to check the state of seed pod. You should only see the heart beat state of seed pod.

- Edit the nonseed-deployment.yml and add the IP address of seed pod in environment variables. Run the nonseed-deployment.yml

- Now recheck the state of seed pod (using GET /gossip/mystate) and you should be able to see non seed pods heartbeats in the state. Gossip Protocol has lead to node discovery.



## References

- https://www.linkedin.com/pulse/gossip-protocol-inside-apache-cassandra-soham-saha

- https://youtu.be/FuP1Fvrv6ZQ

- https://medium.com/@swarnimsinghal/implementing-cassandras-gossip-protocol-part-1-b9fd161e5f49

- https://medium.com/@swarnimsinghal/implementing-cassandras-gossip-protocol-part-2-56332db9177

