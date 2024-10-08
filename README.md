# FC-Kafka-Ecosystem
## Part 06 - Deploying and Operating Kafka in K8S Environment


### Initialize Environment Variables
```
export PROJECT_ID=<project-id> 
export KUBERNETES_CLUSTER_PREFIX=kafka
export REGION=us-west1
```


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

gcloud container clusters get-credentials ${KUBERNETES_CLUSTER_PREFIX}-cluster --region ${REGION}

kubectl create ns kafka

helm install strimzi-operator strimzi/strimzi-kafka-operator -n kafka
```

### Apply Kafka.yaml
```
kubectl apply -f kafka.yaml
```

### Run Kafka Client 
```
kubectl run kafka-client -n kafka  -ti --image bitnami/kafka:3.5.1 -- bash
```

### Test with client 
```
kafka-topics.sh --version --bootstrap-server <BOOTSTRAP_CLUSTER_IP>:9092
kafka-topics.sh --list --bootstrap-server <BOOTSTRAP_CLUSTER_IP>:9092
```

## CH03	Operating Kafka Cluster Using Console Tools



### Topic Management
```
List down all pods: kubectl get pods -n kafka
Exec kafka client: kubectl exec -it kafka-client -n kafka -- bash

1. Show all kafka topics: 
    kafka-topics.sh --bootstrap-server <kafka-broker> --list
2. Create kafka topic: 
    kafka-topics.sh --create --bootstrap-server <kafka-broker> --replication-factor 3 --partitions 3 --topic my-topic
3. Describe kafka topic: 
    kafka-topics.sh --describe --topic my-topic --bootstrap-server <kafka-broker>
4. DELETE topic: 
    kafka-topics.sh --delete --topic my-topic --bootstrap-server <kafka-broker> 
5. Describe config: 
    kafka-configs.sh --bootstrap-server my-cluster-kafka-bootstrap.kafka.svc:9092 --entity-type topics --entity-name my-topic --describe

6. Sample Configs: 
    kafka-configs.sh --bootstrap-server my-cluster-kafka-bootstrap.kafka.svc:9092 --alter --entity-type topics --entity-name my-topic --add-config retention.ms=86400000
    kafka-configs.sh --bootstrap-server my-cluster-kafka-bootstrap.kafka.svc:9092 --alter --entity-type topics --entity-name my-topic --add-config compression.type=snappy
    kafka-configs.sh --bootstrap-server my-cluster-kafka-bootstrap.kafka.svc:9092 --alter --entity-type topics --entity-name my-topic --add-config segment.bytes=1073741824
    kafka-configs.sh --bootstrap-server my-cluster-kafka-bootstrap.kafka.svc:9092 --alter --entity-type topics --entity-name my-topic --add-config cleanup.policy=compact
    kafka-configs.sh --bootstrap-server my-cluster-kafka-bootstrap.kafka.svc:9092 --alter --entity-type topics --entity-name my-topic --add-config min.insync.replicas=2
    kafka-configs.sh --bootstrap-server my-cluster-kafka-bootstrap.kafka.svc:9092 --alter --entity-type topics --entity-name my-topic --add-config max.message.bytes=10485760
    kafka-configs.sh --bootstrap-server my-cluster-kafka-bootstrap.kafka.svc:9092 --alter --entity-type topics --entity-name my-topic --add-config message.timestamp.difference.max.ms=604800000
    kafka-configs.sh --bootstrap-server my-cluster-kafka-bootstrap.kafka.svc:9092 --alter --entity-type topics --entity-name my-topic --add-config flush.messages=10000
```

### Consumer Group Management
```
1. List all consumer groups in the cluster: 
  kafka-consumer-groups.sh --bootstrap-server <kafka-broker> --list

2. Start consumer to automatically create consumer group 
  kafka-console-consumer.sh --bootstrap-server <kafka-broker> --topic my-topic --group my-consumer-group

3. Describing Consumer Group Details
  kafka-consumer-groups.sh --bootstrap-server <kafka-broker> --describe --group <consumer-group>

4. Resetting Consumer Offsets
  kafka-consumer-groups.sh --bootstrap-server <kafka-broker> --group <consumer-group> --reset-offsets --to-earliest --execute --topic my-topic

5. Delete Consumer Group 
  kafka-consumer-groups.sh --bootstrap-server <kafka-broker> --delete --group <consumer-group>

```


### Upgrade Kafka
```
Check current version: 
    kubectl get kafka my-cluster -o yaml

Check the state of your topics and partitions:
    kubectl exec -n kafka kafka-client -- kafka-topics.sh --describe --bootstrap-server my-cluster-kafka-bootstrap.kafka.svc:9092

Check consumer group lags:
    kubectl exec -n kafka kafka-client -- kafka-consumer-groups.sh --describe --all-groups --bootstrap-server my-cluster-kafka-bootstrap.kafka.svc:9092
```












### Security SASL/SCRAM
Check Security Configurations: 
```
kafka-console-producer.sh --broker-list <broker-host>:<port> --topic <topic-name> --producer-property security.protocol=SASL_PLAINTEXT --producer-property sasl.mechanism=SCRAM-SHA-256
```

Setting on the Kafka broker: 
```
1. Connect to your Kafka broker pod: 
  kubectl exec -it my-cluster-kafka-0 -n kafka -- bash
  
2. Edit the Kafka broker configuration file (server.properties):
    Locate the server.properties file, typically under /opt/kafka/config/ or similar.
    Modify the server.properties to enable SASL/SCRAM authentication mechanisms: 
        listeners=SASL_PLAINTEXT://0.0.0.0:9092
        advertised.listeners=SASL_PLAINTEXT://<broker-ip>:9092
        sasl.enabled.mechanisms=SCRAM-SHA-256,SCRAM-SHA-512
3. Set up the JAAS config for SCRAM:
    Add the JAAS configuration for SCRAM in server.properties:        
        sasl.jaas.config=org.apache.kafka.common.security.scram.ScramLoginModule required username="scram-user" password="user-secret";
4. exit   
```
Create Kafka User with SCRAM Credentials
```
Exec into strimzi pod: 
    kubectl exec -it <strimzi-operator-pod> -n kafka -- /bin/bash

Create SCRAM user:
    - create kafka-user-scram.yaml 
    - kubectl apply -f kafka-user-scram.yaml
    - Verify the user creation: kubectl get kafkauser -n kafka
```

Restart Kafka Broker
```
kubectl delete pod my-cluster-kafka-0 -n kafka
or 
kubectl rollout restart statefulset <kafka-broker-statefulset-name> -n kafka
```

Produce Message: 
```
kafka-console-producer.sh \
--broker-list <broker-host>:9092 \
--topic my-topic \
--producer-property security.protocol=SASL_PLAINTEXT \
--producer-property sasl.mechanism=SCRAM-SHA-256 \
--producer-property sasl.jaas.config="org.apache.kafka.common.security.scram.ScramLoginModule required username='scram-user' password='user-secret';"
```

Kafka client to test
```
kafka-console-consumer.sh \
--bootstrap-server <broker-host>:9092 \
--topic my-topic \
--consumer-property security.protocol=SASL_PLAINTEXT \
--consumer-property sasl.mechanism=SCRAM-SHA-256 \
--consumer-property sasl.jaas.config="org.apache.kafka.common.security.scram.ScramLoginModule required username='scram-user' password='user-secret';" \
--from-beginning

```

### Configuring Delegation Token
```
Exec into broker pod: 
    kubectl exec -it my-cluster-kafka-0 -n kafka -- bash

Edit the server.properties file to Configuring Delegation Token: 
    delegation.token.enabled=true
    delegation.token.max.lifetime.ms=86400000  # 1 day
    delegation.token.lifetime.ms=3600000  # 1 hour

Issue a Token:    
    kafka-delegation-token.sh --create --owner <username> --token-lifetime 3600    
    
Renewing Delegation Tokens:
    kafka-delegation-token.sh --renew --token <token-id> --renew-time <time-in-seconds>

Canceling a Token:
    kafka-delegation-token.sh --cancel --token <token-id>    
```


### SASL/OAUTHBEARER Authentication
Enable SASL/OAUTHBEARER in Kafka
on server.properties: 
```
# SASL listener configuration
listeners=SASL_PLAINTEXT://<broker-host>:<port>

# Enable OAUTHBEARER as an authentication mechanism
sasl.enabled.mechanisms=OAUTHBEARER

# Specify the OAuth provider's token endpoint URL
sasl.oauthbearer.token.endpoint.url=https://<oauth-provider>/token

# Optional: SSL configuration if using SASL_SSL instead of SASL_PLAINTEXT
# ssl.keystore.location=/path/to/keystore.jks
# ssl.keystore.password=<keystore-password>
# ssl.key.password=<key-password>
# ssl.truststore.location=/path/to/truststore.jks
# ssl.truststore.password=<truststore-password>

# Optional: Adjust token refresh behavior (on clients)
# sasl.oauthbearer.jwks.endpoint.refresh.ms=3600000


sasl.jaas.config=org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required \
oauth.token.endpoint.uri="https://<oauth-provider>/token" \
client.id="<client-id>" \
client.secret="<client-secret>";


Restart Kafka Broker with new configuration

Test Config
kafka-console-producer.sh --broker-list <broker-host>:<port> --topic my-topic \
--producer-property security.protocol=SASL_PLAINTEXT \
--producer-property sasl.mechanism=OAUTHBEARER
```


### ACL (Access Control Lists)
```
kubectl exec -it <kafka-broker-pod> -n kafka-- /bin/bash
Check ACLs: 
    ./kafka-acls.sh --bootstrap-server <kafka-broker-host>:<kafka-broker-port> --list --topic my-topic



``` 


### Client Quotas 
Edit the server.properties file
```
quota.producer.default=1024000  # 1 MB/s for producers
quota.consumer.default=1024000  # 1 MB/s for consumers
```

Specify Client-Specific Quotas: 
```
quota.producer.<client-id>=512000  # 512 KB/s for a specific producer client
quota.consumer.<client-id>=512000   # 512 KB/s for a specific consumer client
```

## Monitoring and Operation Tools Prometheus
Create a namespace for monitoring
```
 kubectl create namespace monitoring
 ```

Create and apply prometheus-operator-deployment.yaml
```
nano prometheus-operator-deployment.yaml
kubectl apply -f prometheus-operator-deployment.yaml -n monitoring --force-conflicts=true --server-side
```

Create and apply prometheus.yaml 
```
nano prometheus.yaml
kubectl apply -f prometheus.yaml -n monitoring
```

Create and apply strimzi-pod-monitor.yaml
``` 
nano strimzi-pod-monitor.yaml
kubectl apply -f strimzi-pod-monitor.yaml -n monitoring
```

Create and apply grafana.yaml
```
nano grafana.yaml
kubectl apply -f grafana.yaml -n monitoring
```

Forward Grafana & Prometheus-operated Service Port
```
kubectl port-forward svc/grafana 3001:3000 -n monitoring & 
kubectl port-forward svc/prometheus-operated 9090:9090 -n monitoring &
```
note: if error from kafka, you can reapply the kafka using kafka-v2.yaml

### Changing Number of Partitions

my-topic.yaml
```
apiVersion: kafka.strimzi.io/v1beta2
kind: KafkaTopic
metadata:
  name: my-topic  # Change to your desired topic name
  namespace: kafka  # Ensure this matches your Kafka namespace
spec:
  partitions: 5  # Desired number of partitions
  replicas: 3    # Desired replication factor

```

Command to change number of partition and replicas
```
kubectl apply -f - <<EOF
apiVersion: kafka.strimzi.io/v1beta2
kind: KafkaTopic
metadata:
  name: my-topic
  namespace: kafka
spec:
  partitions: 2
  replicas: 4
EOF

```

List kafka topic
```
kubectl get kafkatopics -n kafka
```

Leader Election
```
kubectl exec my-cluster-controller-3 -n kafka -- kafka-topics.sh --zookeeper my-cluster-zookeeper:2181 --describe --topic my-topic
```

z