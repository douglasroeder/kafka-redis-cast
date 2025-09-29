package au.mandrakez.redis;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;

import java.util.Map;

public class RedisSinkConnectorConfig extends AbstractConfig {

    public static final String REDIS_HOST_CONFIG = "redis.host";
    private static final String REDIS_HOST_DOC = "Redis server hostname.";

    public static ConfigDef config() {
        return new ConfigDef()
                .define(REDIS_HOST_CONFIG, ConfigDef.Type.STRING, "localhost", ConfigDef.Importance.HIGH, REDIS_HOST_DOC);
    }

    public RedisSinkConnectorConfig(Map<String, String> parsedConfig) {
        super(config(), parsedConfig);
    }
}