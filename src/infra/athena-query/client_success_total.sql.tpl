WITH metrics AS (
  SELECT
    cast(COALESCE(
      try(from_unixtime(try_cast(cast(timestamp AS varchar) AS bigint) / 1000.0)),
      try(from_iso8601_timestamp(cast(timestamp AS varchar)))
    ) AS date) AS event_date,
    try_cast(value AS double) AS metric_value
  FROM "${database_name}"."${catalog_table_name}"
  WHERE metric_name = 'ClientSuccess'
)
SELECT
  sum(metric_value) AS total_successful_logins
FROM metrics
WHERE event_date BETWEEN DATE '2025-03-01' AND DATE '2026-08-31'
  AND metric_value IS NOT NULL;