# FC-Kafka-Ecosystem

## Building a Content Platform Using Kafka

### Environment Setup
- Java 17
- Maven 3.8.3
- Docker
- Docker Compose
- Zookeeper
- Kafka
- MySQL
- MongoDB
- Redis
- ElasticSearch

### How to Run
- Run Docker Compose
```shell
docker-compose -f docker-compose.yml up
```
- Make sure all services are up and running, check using
```shell
docker-compose ps
```
- Run the application using intelliJ or using the following command, go to each project and run the following commands
```shell
 mvn clean install -Dmaven.test.skip=true
 mvn spring-boot:run
```