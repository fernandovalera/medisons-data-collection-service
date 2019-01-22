CREATE TABLE IF NOT EXISTS signal_info (
  name              VARCHAR(32) NOT NULL PRIMARY KEY,
  frequency         DOUBLE NOT NULL
);

CREATE TABLE IF NOT EXISTS spo2 (
  timestamp         BIGINT UNSIGNED NOT NULL PRIMARY KEY,
  value             DOUBLE NOT NULL
);

CREATE TABLE IF NOT EXISTS spo2_score (
  timestampFrom     BIGINT UNSIGNED NOT NULL,
  timestampTo       BIGINT UNSIGNED NOT NULL,
  value             DOUBLE NOT NULL,
  CONSTRAINT PK_spo2_score PRIMARY KEY (timestampFrom, timestampTo)
);