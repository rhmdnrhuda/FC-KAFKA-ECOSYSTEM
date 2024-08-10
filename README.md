# FC-Kafka-Ecosystem
## Part 01 - CH 02 : Basic Concepts and Understanding of Apache Kafka

### Demo Producer and Consumer
- Navigate to the path where the **docker-compose.yml** is located and then run the below command.
```
docker-compose up
```

## Producer and Consume the Messages

- Let's going to the container by running the below command.

```
docker exec -it kafka1 bash
```

- Create a Kafka topic using the **kafka-topics** command.
    - **kafka1:19092** refers to the **KAFKA_ADVERTISED_LISTENERS** in the docker-compose.yml file.

```
kafka-topics --bootstrap-server kafka1:19092 \
             --create \
             --topic test-topic \
             --replication-factor 1 --partitions 1
```

- Produce Messages to the topic.

```
docker exec --interactive --tty kafka1  \
kafka-console-producer --bootstrap-server kafka1:19092 \
                       --topic test-topic
```

- Consume Messages from the topic.

```
docker exec --interactive --tty kafka1  \
kafka-console-consumer --bootstrap-server kafka1:19092 \
                       --topic test-topic \
                       --from-beginning
```

## Producer and Consume the Messages With Key and Value

- Produce Messages with Key and Value to the topic.

```
docker exec --interactive --tty kafka1  \
kafka-console-producer --bootstrap-server kafka1:19092 \
                       --topic test-topic \
                       --property "key.separator=-" --property "parse.key=true"
```

- Consuming messages with Key and Value from a topic.

```
docker exec --interactive --tty kafka1  \
kafka-console-consumer --bootstrap-server kafka1:19092 \
                       --topic test-topic \
                       --from-beginning \
                       --property "key.separator= - " --property "print.key=true"
```

### Consume Messages using Consumer Groups


```
docker exec --interactive --tty kafka1  \
kafka-console-consumer --bootstrap-server kafka1:19092 \
                       --topic test-topic --group console-consumer-41911\
                       --property "key.separator= - " --property "print.key=true"
```

- Example Messages:

```
a-abc
b-bus
```

### Consume Messages With Headers

```
docker exec --interactive --tty kafka1  \
kafka-console-consumer --bootstrap-server kafka1:19092 \
                       --topic library-events.DLT \
                       --property "print.headers=true" --property "print.timestamp=true" 
```

- Example Messages:

```
a-abc
b-bus
```

