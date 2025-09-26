package au.seek.redis;

import java.util.List;

public interface RedisClient {
    void set(String key, String value);
    String get(String key);
    // Add other Redis commands as needed

    // Transaction-related methods
    void multi(); // Initiates a transaction
    void enqueueIncr(String key); // Enqueues a SET command
    void enqueueSAdd(String key, String member);
    List<Object> exec(); // Executes the transaction
    void discard(); // Discards the transaction
    void close();
}
