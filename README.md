# FC-Kafka-Ecosystem
## Part 04 - CH 05 : Kafka Sink Connector Practice

### build the image with the following command:
```
docker build -t custom-kafka-connect-image -f Dockerfile.kafka-connect .
```

### Create table to save data from kafka
```
CREATE TABLE kafka_messages (
                                id INT PRIMARY KEY,
                                message TEXT NOT NULL
);
```

### Check installed connector plugins
```
curl --location 'http://localhost:8083/connector-plugins'

```
sample response when success:
```
[
    {
        "class": "io.debezium.connector.mysql.MySqlConnector",
        "type": "source",
        "version": "2.2.0.Final"
    },
    {
        "class": "org.apache.kafka.connect.mirror.MirrorCheckpointConnector",
        "type": "source",
        "version": "7.3.2-ccs"
    },
    {
        "class": "org.apache.kafka.connect.mirror.MirrorHeartbeatConnector",
        "type": "source",
        "version": "7.3.2-ccs"
    },
    {
        "class": "org.apache.kafka.connect.mirror.MirrorSourceConnector",
        "type": "source",
        "version": "7.3.2-ccs"
    }
]
```

### Curl to validate debezium connector for mysql
```
curl --location --request PUT 'http://localhost:8083/connector-plugins/io.debezium.connector.mysql.MySqlConnector/config/validate' \
--header 'Content-Type: application/json' \
--data '{
  "connector.class": "io.debezium.connector.mysql.MySqlConnector",
  "tasks.max": "1",
  "database.hostname": "mysql-db-host",
  "database.port": "3306",
  "database.user": "dbuser",
  "database.password": "dbpassword",
  "database.server.id": "184054",
  "database.server.name": "dbserver1",
  "database.whitelist": "testdb",
  "table.whitelist": "testdb.testtable",
  "topic.prefix": "mysql-testdb-"
}'
```

### CURL to create a connector: mysql-sink-connector
```
curl --location 'http://localhost:8083/connectors' \
--header 'Content-Type: application/json' \
--data '{
    "name": "mysql-sink-connector",
    "config": {
        "connector.class": "io.confluent.connect.jdbc.JdbcSinkConnector",
        "tasks.max": "1",
        "topics": "jdbd-kafka-sync",
        "connection.url": "jdbc:mysql://mysql-container:3306/testdb?user=testuser&password=testpassword",
        "insert.mode": "insert",
        "auto.create": "true",
        "auto.evolve": "false",
        "pk.mode": "none",
        "table.name.format": "kafka_messages",
        "key.converter": "org.apache.kafka.connect.storage.StringConverter",
        "value.converter": "org.apache.kafka.connect.json.JsonConverter",
        "value.converter.schemas.enable": "true",
        "connect.classpath": "/usr/share/java/mysql-connector-java-8.0.30.jar"
    }
}'
```

### CURL to update config connector based on the connector name
```
curl --location --request PUT 'http://localhost:8083/connectors/mysql-sink-connector/config' \
--header 'Content-Type: application/json' \
--data '{
  "connector.class": "io.confluent.connect.jdbc.JdbcSinkConnector",
  "tasks.max": "1",
  "topics": "jdbd-kafka-sync",
  "connection.url": "jdbc:mysql://mysql-container:3306/testdb?user=testuser&password=testpassword",
  "insert.mode": "insert",
  "auto.create": "true",
  "auto.evolve": "false",
  "pk.mode": "none",
  "table.name.format": "kafka_messages",
  "key.converter": "org.apache.kafka.connect.storage.StringConverter",
  "value.converter": "org.apache.kafka.connect.json.JsonConverter",
  "value.converter.schemas.enable": "true",
  "connect.classpath": "/usr/share/java/mysql-connector-java-8.0.30.jar"
}'
```


### Grant access the user to the database
```
1. docker exec -it mysql-container mysql -u root -p 
2. root password: rootpassword (inside the docker-compose file)
3. GRANT RELOAD, FLUSH_TABLES, REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO 'testuser'@'%'; 
FLUSH PRIVILEGES;
4. Restart the Debezium Connector using curl command

```

### Restart connector
```
curl --location --request POST 'http://localhost:8083/connectors/mysql-sink-connector/restart'
```
