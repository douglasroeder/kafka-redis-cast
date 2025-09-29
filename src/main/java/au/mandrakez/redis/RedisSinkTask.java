package au.mandrakez.redis;

import org.apache.kafka.connect.sink.SinkRecord;
import org.apache.kafka.connect.sink.SinkTask;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class RedisSinkTask extends SinkTask {

    protected RedisClient redisClient;
    private String redisHost;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String version() {
        return "1.0";
    }

    @Override
    public void start(Map<String, String> props) {
        redisHost = props.get(RedisSinkConnectorConfig.REDIS_HOST_CONFIG);
        this.redisClient = new JedisRedisClient(redisHost, 6379);
    }

    @Override
    public void put(Collection<SinkRecord> records) {
        for (SinkRecord record : records) {
            try {
                String value = record.value().toString();
                JsonNode jsonNode = objectMapper.readTree(value);

                JsonNode candidateIdNode = jsonNode.get("candidateId");
                JsonNode jobIdNode = jsonNode.get("jobId");
                JsonNode eventNode = jsonNode.get("event");

                if (candidateIdNode == null || jobIdNode == null || eventNode == null) {
                    System.err.println("Missing required fields in JSON: " + value);
                    continue;
                }

                String candidateKey = String.format("c:%s:e:%s", candidateIdNode.asText(),  eventNode.asText());
                String jobKey = String.format("j:%s:e:%s", jobIdNode.asText(),  eventNode.asText());
                String candidateJobKey = String.format("c:%s:e:%s:j:%s", candidateIdNode.asText(),  eventNode.asText(), jobIdNode.asText());

                // Start transaction
                redisClient.multi();

                // count by Candidate ID
                redisClient.enqueueIncr(candidateKey);

                // count by Job ID
                redisClient.enqueueIncr(jobKey);

                // count by Candidate + Job ID
                redisClient.enqueueIncr(candidateJobKey);

                // Set for interacted jobs per candidate and event
                String candidateJobsSetKey = String.format("c:%s:e:%s:jobs", candidateIdNode.asText(), eventNode.asText());
                redisClient.enqueueSAdd(candidateJobsSetKey, jobIdNode.asText());

                // Set for interacted candidates per job and event
                String jobCandidatesSetKey = String.format("j:%s:e:%s:candidates", jobIdNode.asText(), eventNode.asText());
                redisClient.enqueueSAdd(jobCandidatesSetKey, candidateIdNode.asText());

                // Execute
                List<Object> results = redisClient.exec();

                System.out.println("INCR " + candidateKey);
                System.out.println("INCR " + jobKey);

                System.out.println("Transaction Results: " + results);

            } catch (Exception e) {
                System.err.println("Failed to process record: " + record.value());
                e.printStackTrace();
            }
        }
    }

    @Override
    public void stop() {
        if (redisClient != null) {
            redisClient.close();
        }
    }
}