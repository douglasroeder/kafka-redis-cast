package au.mandrakez.redis;

import org.apache.kafka.connect.sink.SinkRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

class RedisSinkTaskTest {

    private RedisSinkTask task;
    private RedisClient mockRedisClient;

    @BeforeEach
    void setUp() {
        mockRedisClient = mock(RedisClient.class);
        task = new RedisSinkTask();
        task.redisClient = mockRedisClient; // Inject the mock RedisClient
    }

    @Test
    void testPut_shouldIncrementAndAddToSets() throws Exception {
        // Arrange: JSON payload matching your new logic
        String json = "{\"candidateId\": 123, \"jobId\": 321, \"event\": \"click\"}";
        SinkRecord record = new SinkRecord("test-topic", 0, null, null, null, json, 0);

        when(mockRedisClient.exec()).thenReturn(List.of(1L, 1L, 1L, 1L, 1L)); // Dummy results for 5 operations

        // Act
        task.put(Collections.singletonList(record));

        // Assert: verify transaction methods
        verify(mockRedisClient).multi();

        // Verify enqueueIncr called 3 times with the expected keys
        ArgumentCaptor<String> incrCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockRedisClient, times(3)).enqueueIncr(incrCaptor.capture());

        List<String> incrKeys = incrCaptor.getAllValues();
        // Check expected keys
        assert incrKeys.contains("c:123:e:click");
        assert incrKeys.contains("j:321:e:click");
        assert incrKeys.contains("c:123:e:click:j:321");

        // Verify enqueueSAdd called 2 times with expected keys and values
        ArgumentCaptor<String> saddKeyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> saddMemberCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockRedisClient, times(2)).enqueueSAdd(saddKeyCaptor.capture(), saddMemberCaptor.capture());

        List<String> saddKeys = saddKeyCaptor.getAllValues();
        List<String> saddMembers = saddMemberCaptor.getAllValues();

        // Expected set keys and members
        assert saddKeys.contains("c:123:e:click:jobs");
        assert saddKeys.contains("j:321:e:click:candidates");
        assert saddMembers.contains("321"); // jobId added to candidate's jobs set
        assert saddMembers.contains("123"); // candidateId added to job's candidates set

        // Verify exec called once to execute the transaction
        verify(mockRedisClient).exec();
    }
}