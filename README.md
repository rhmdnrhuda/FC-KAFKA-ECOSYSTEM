# FC-Kafka-Ecosystem
## Part 06 

### Create a Kubernetes Cluster
```
gcloud container clusters create ${KUBERNETES_CLUSTER_PREFIX}-cluster \
  --region ${REGION} \
  --machine-type e2-small \
  --num-nodes 1
```


### Install Strimzi Operator
```
helm repo add strimzi https://strimzi.io/charts/

kubectl create ns kafka

helm install strimzi-operator strimzi/strimzi-kafka-operator -n kafka
```

### Kafka.yaml
```
apiVersion: kafka.strimzi.io/v1beta2
kind: KafkaNodePool
metadata:
 name: controller
 labels:
   strimzi.io/cluster: my-cluster
spec:
 replicas: 3
 roles:
   - controller
 storage:
   type: jbod
   volumes:
     - id: 0
       type: persistent-claim
       size: 20Gi
       deleteClaim: false
---

apiVersion: kafka.strimzi.io/v1beta2
kind: KafkaNodePool
metadata:
 name: broker
 labels:
   strimzi.io/cluster: my-cluster
spec:
 replicas: 3
 roles:
   - broker
 storage:
   type: jbod
   volumes:
     - id: 0
       type: persistent-claim
       size: 100Gi
       deleteClaim: false
---

apiVersion: kafka.strimzi.io/v1beta2
kind: Kafka
metadata:
 name: my-cluster
 annotations:
   strimzi.io/node-pools: enabled
   strimzi.io/kraft: enabled
spec:
 kafka:
   version: 3.8.0
   metadataVersion: 3.5-IV2
   # The replicas field is required by the Kafka CRD schema while the KafkaNodePools feature gate is in alpha phase.
   # But it will be ignored when Kafka Node Pools are used
  #  replicas: 3
   listeners:
     - name: plain
       port: 9092
       type: internal
       tls: false
     - name: tls
       port: 9093
       type: internal
       tls: true
   config:
     offsets.topic.replication.factor: 3
     transaction.state.log.replication.factor: 3
     transaction.state.log.min.isr: 2
     default.replication.factor: 3
     min.insync.replicas: 2
```

### Apply Kafka.yaml
```
kubectl apply -f kafka.yaml
```

### Run Kafka Client 
```
kubectl run kafka-client -n kafka --rm -ti --image bitnami/kafka:3.5.1 -- bash
```

### Test with client 
```
kafka-topics.sh --version --bootstrap-server <BOOTSTRAP_CLUSTER_IP>:9092
kafka-topics.sh --list --bootstrap-server <BOOTSTRAP_CLUSTER_IP>:9092
```