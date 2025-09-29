package au.mandrakez.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.List;

public class JedisRedisClient implements RedisClient {
    private final Jedis jedis;
    private Transaction transaction;

    public JedisRedisClient(String host, int port) {
        this.jedis = new Jedis(host, port);
    }

    @Override
    public void set(String key, String value) {
        jedis.set(key, value);
    }

    @Override
    public String get(String key) {
        return jedis.get(key);
    }

    // Implement other Redis commands directly if not part of a transaction

    @Override
    public void multi() {
        this.transaction = jedis.multi();
    }

    @Override
    public void enqueueIncr(String key) {
        if (transaction == null) {
            throw new IllegalStateException("Transaction not initiated. Call multi() first.");
        }
        transaction.incr(key);
    }

    @Override
    public void enqueueSAdd(String key, String member) {
        if (transaction == null) {
            throw new IllegalStateException("Transaction not initiated. Call multi() first.");
        }
        transaction.sadd(key, member);
    }

    @Override
    public List<Object> exec() {
        if (transaction == null) {
            throw new IllegalStateException("Transaction not initiated. Call multi() first.");
        }
        List<Object> results = transaction.exec();
        this.transaction = null; // Reset transaction after execution
        return results;
    }

    @Override
    public void discard() {
        if (transaction != null) {
            transaction.discard();
            this.transaction = null; // Reset transaction after discard
        }
    }

    @Override
    public void close() {
        jedis.close();
    }
}
