# FC-Kafka-Ecosystem
## Part 04 - CH 06 : Introduction and Configuration Practice of MirrorMaker2

### build the image with the following command:
```
docker build -t custom-kafka-connect-image -f Dockerfile.kafka-connect .
```

### Create multiple Cluster
```
refer to the docker-compose.yml file
```

### Create a topic in the source cluster
```
docker exec -it kafka1 kafka-topics --create --topic test-topic --bootstrap-server kafka1:19092 --partitions 1 --replication-factor 1
```


### Produce Messages to the source topic
```
docker exec -it kafka1 kafka-console-producer --topic test-topic --bootstrap-server kafka1:19092
```

### Check the Replicated Topic in the Target Cluster
```
docker exec -it kafka2 kafka-topics --describe --topic test-topic --bootstrap-server kafka2:19092
```

### Consume Messages from the Target Topic
```
docker exec -it kafka2 kafka-console-consumer --topic test-topic --bootstrap-server kafka2:19092 --from-beginning
```
