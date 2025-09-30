# AWS MSK Cluster
resource "aws_msk_cluster" "kafka" {
  cluster_name           = "my-msk-cluster"
  kafka_version          = "3.4.0"
  number_of_broker_nodes = 3
  broker_node_group_info {
    instance_type   = "kafka.m5.large"
    client_subnets  = [aws_subnet.subnet1.id, aws_subnet.subnet2.id]
    security_groups = [aws_security_group.kafka.id]
  }
}

# AWS MemoryDB Cluster
resource "aws_memorydb_cluster" "redis" {
  cluster_name = "my-memorydb-cluster"
  node_type    = "db.r6g.large"
  num_shards   = 1
  num_replicas_per_shard = 1
  subnet_group_name = aws_memorydb_subnet_group.redis_subnet_group.name
  security_group_ids = [aws_security_group.redis.id]
}

# AWS S3 Bucket for MSK Connect Plugin
resource "aws_s3_bucket" "plugin_bucket" {
  bucket = "my-redis-plugin-bucket"

  tags = {
    Name        = "Redis Plugin Bucket"
    Environment = "dev"
  }
}

# AWS MSK Connect Custom Plugin
resource "aws_mskconnect_custom_plugin" "redis_plugin" {
  name = "redis-plugin"
  content_type = "JAR"
  location {
    s3 {
      bucket_arn = aws_s3_bucket.plugin_bucket.arn
      file_key   = "kafka-redis-connector-fat.jar"
    }
  }
}

# AWS MSK Connect Connector
resource "aws_mskconnect_connector" "redis_sink" {
  name = "redis-sink-connector"
  kafka_cluster {
    apache_kafka_cluster {
      bootstrap_servers = aws_msk_cluster.kafka.bootstrap_brokers
      vpc {
        security_groups = [aws_security_group.kafka.id]
        subnets         = [aws_subnet.subnet1.id, aws_subnet.subnet2.id]
      }
    }
  }
  connector_configuration = {
    "connector.class" = "au.mandrakez.redis.RedisSinkConnector"
    "tasks.max"       = "1"
    "topics"          = "my-topic"
    "redis.host"      = aws_memorydb_cluster.redis.cluster_endpoint[0].address
  }
  plugin {
    custom_plugin {
      arn = aws_mskconnect_custom_plugin.redis_plugin.arn
    }
  }
}
