# Kafka Connect | custom Redis plugin

### Usefull commands

```bash
# Build and Test Java plugin
mvn clean package

# Start Kafka / Redis / Zookeeper
docker-compose up -d

# Follow Kakfa logs for debugging
docker-compose logs -f kafka-connect

# Register Custom Kafka Connect plugin
curl -X PUT http://localhost:8083/connectors \
     -H "Content-Type: application/json" \
     -d @redis-sink-config.json

# Create my-topic for testing purposes
docker exec -it kafka-redis-cast-kafka-1 kafka-topics --create \
  --bootstrap-server kafka:29092 \
  --replication-factor 1 --partitions 1 \
  --topic my-topic

# Create required connect topics for docker-compose setup
docker exec -it kafka-redis-cast-kafka-1 kafka-topics \
  --create \
  --bootstrap-server kafka:29092 \
  --replication-factor 1 \
  --partitions 1 \
  --topic connect-configs \
  --config cleanup.policy=compact
  
docker exec -it kafka-redis-cast-kafka-1 kafka-topics \
  --create \
  --bootstrap-server kafka:29092 \
  --replication-factor 1 \
  --partitions 1 \
  --topic connect-offsets \
  --config cleanup.policy=compact
  
docker exec -it kafka-redis-cast-kafka-1 kafka-topics \
  --create \
  --bootstrap-server kafka:29092 \
  --replication-factor 1 \
  --partitions 1 \
  --topic connect-status \
  --config cleanup.policy=compact

# Push message to Kafka topic
echo '{"candidateId": 593, "jobId": 323, "event": "impression"}' | \
  docker exec -i kafka-redis-cast-kafka-1 bash -c \
  "kafka-console-producer --broker-list localhost:9092 --topic my-topic"

# Load LUA function into redis
cat get_jobs_for_candidates.lua | redis-cli -x FUNCTION LOAD REPLACE

# Call Redis function passing arguments
redis-cli FCALL get_jobs_for_candidate 1 "" 123 impression
=> "{\"candidateId\":\"123\",\"event\":\"impression\",\"jobIds\":[\"321\",\"322\",\"323\"]}"
```