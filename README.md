# FC-Kafka-Ecosystem
## Part 04 - CH 03 : Basic Concepts and Understanding of Apache Kafka


### build the image with the following command:
```
docker build -t custom-kafka-connect-image -f Dockerfile.kafka-connect .
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

### CURL to create a connector: mysql-connector
```
curl --location 'http://localhost:8083/connectors' \
--header 'Content-Type: application/json' \
--data '{
  "name": "mysql-source-connector",
  "config": {
    "connector.class": "io.debezium.connector.mysql.MySqlConnector",
    "tasks.max": "1",
    "database.hostname": "mysql-container",
    "database.port": "3306",
    "database.user": "testuser",
    "database.password": "testpassword",
    "database.server.id": "184054",
    "database.server.name": "dbserver1",
    "database.whitelist": "testdb",
    "table.whitelist": "testdb.testtable",
    "topic.prefix": "mysql-testdb-"
  }
}
'
```

### CURL to update config connector based on the connector name
```
curl --location --request PUT 'http://localhost:8083/connectors/mysql-source-connector/config' \
--header 'Content-Type: application/json' \
--data '{
  "connector.class": "io.debezium.connector.mysql.MySqlConnector",
  "tasks.max": "1",
  "database.hostname": "mysql-container",
  "database.port": "3306",
  "database.user": "testuser",
  "database.password": "testpassword",
  "database.server.id": "184054",
  "database.server.name": "dbserver1",
  "database.whitelist": "testdb",
  "table.whitelist": "testdb.testtable",
  "topic.prefix": "mysql-testdb-",
  "schema.history.internal.kafka.bootstrap.servers": "kafka1:19092",
  "schema.history.internal.kafka.topic": "dbserver1-schema-history",
  "database.allowPublicKeyRetrieval": "true"
}'
```

### Create table and insert samples data
```
CREATE TABLE testtable (
                           id INT AUTO_INCREMENT PRIMARY KEY,
                           data VARCHAR(255) NOT NULL
);

INSERT INTO testtable (data) VALUES ('Sample Data 3'), ('Sample Data 4');
```


### Grant access the user to the database
```
1. docker exec -it mysql-container mysql -u root -p 
2. root password: rootpassword (inside the docker-compose file)
3. GRANT RELOAD, FLUSH_TABLES, REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO 'testuser'@'%'; 
FLUSH PRIVILEGES;
4. Restart the Debezium Connector using curl command

```

### Restart Debezium connector
```
curl --location --request POST 'http://localhost:8083/connectors/mysql-source-connector/restart'
```
