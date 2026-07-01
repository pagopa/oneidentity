resource "aws_security_group" "client_cache" {
  name        = "${var.cache_name}-sg"
  description = "Security group for client configuration cache."
  vpc_id      = var.vpc_id

  tags = merge(var.tags, {
    Name = "${var.cache_name}-sg"
  })
}

resource "aws_vpc_security_group_ingress_rule" "client_cache" {
  for_each = {
    for index, security_group_id in var.allowed_security_group_ids : tostring(index) => security_group_id
  }

  security_group_id            = aws_security_group.client_cache.id
  referenced_security_group_id = each.value
  ip_protocol                  = "tcp"
  from_port                    = 6379
  to_port                      = 6379
  description                  = "Allow Valkey traffic from trusted application security groups."
}

resource "aws_vpc_security_group_egress_rule" "client_cache" {
  security_group_id = aws_security_group.client_cache.id
  ip_protocol       = "-1"
  cidr_ipv4         = "0.0.0.0/0"
  description       = "Allow all outbound traffic."
}

resource "aws_elasticache_serverless_cache" "client_cache" {
  engine = "valkey"
  name   = var.cache_name

  description              = "Serverless Valkey cache for client configurations."
  major_engine_version     = var.major_engine_version
  subnet_ids               = var.subnet_ids
  security_group_ids       = [aws_security_group.client_cache.id]
  snapshot_retention_limit = var.snapshot_retention_limit
  daily_snapshot_time      = var.daily_snapshot_time

  cache_usage_limits {
    data_storage {
      maximum = var.data_storage_maximum_mb
      unit    = "MB"
    }

    ecpu_per_second {
      maximum = var.ecpu_per_second_maximum
    }
  }

  tags = var.tags
}

resource "aws_cloudwatch_metric_alarm" "client_cache" {
  for_each = var.cache_alarms

  alarm_name          = format("%s-%s-%s", var.cache_name, each.value.metric_name, each.value.threshold)
  comparison_operator = each.value.comparison_operator
  evaluation_periods  = each.value.evaluation_periods
  metric_name         = each.value.metric_name
  namespace           = "AWS/ElastiCache"
  period              = each.value.period
  statistic           = each.value.statistic
  threshold           = each.value.threshold
  treat_missing_data  = each.value.treat_missing_data

  dimensions = {
    clusterId = var.cache_name
  }

  alarm_actions = var.alarm_sns_topic_arn != null ? [var.alarm_sns_topic_arn] : []
}
