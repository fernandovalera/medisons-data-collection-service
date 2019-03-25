CREATE TABLE IF NOT EXISTS signal_info (
  name              VARCHAR(32) NOT NULL PRIMARY KEY,
  frequency         DOUBLE NOT NULL
);

CREATE TABLE IF NOT EXISTS bp_sys (
  timestamp         BIGINT UNSIGNED NOT NULL PRIMARY KEY,
  value             DOUBLE NOT NULL
);

CREATE TABLE IF NOT EXISTS bp_dia (
  timestamp         BIGINT UNSIGNED NOT NULL PRIMARY KEY,
  value             DOUBLE NOT NULL
);

CREATE TABLE IF NOT EXISTS bp_score (
  timestampFrom     BIGINT UNSIGNED NOT NULL,
  timestampTo       BIGINT UNSIGNED NOT NULL,
  value             DOUBLE NOT NULL,
  CONSTRAINT PK_bp_score PRIMARY KEY (timestampFrom, timestampTo)
);

CREATE TABLE IF NOT EXISTS ecg (
  timestamp         BIGINT UNSIGNED NOT NULL PRIMARY KEY,
  value             DOUBLE NOT NULL
);

CREATE TABLE IF NOT EXISTS ecg_score (
  timestampFrom     BIGINT UNSIGNED NOT NULL,
  timestampTo       BIGINT UNSIGNED NOT NULL,
  value             DOUBLE NOT NULL,
  CONSTRAINT PK_ecg_score PRIMARY KEY (timestampFrom, timestampTo)
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

CREATE TABLE IF NOT EXISTS resp_rate (
  timestamp         BIGINT UNSIGNED NOT NULL PRIMARY KEY,
  value             DOUBLE NOT NULL
);

CREATE TABLE IF NOT EXISTS resp_rate_score (
  timestampFrom     BIGINT UNSIGNED NOT NULL,
  timestampTo       BIGINT UNSIGNED NOT NULL,
  value             DOUBLE NOT NULL,
  CONSTRAINT PK_resp_rate_score PRIMARY KEY (timestampFrom, timestampTo)
);

CREATE TABLE IF NOT EXISTS temperature (
  timestamp         BIGINT UNSIGNED NOT NULL PRIMARY KEY,
  value             DOUBLE NOT NULL
);

CREATE TABLE IF NOT EXISTS temperature_score (
  timestampFrom     BIGINT UNSIGNED NOT NULL,
  timestampTo       BIGINT UNSIGNED NOT NULL,
  value             DOUBLE NOT NULL,
  CONSTRAINT PK_temperature_score PRIMARY KEY (timestampFrom, timestampTo)
);

CREATE TABLE IF NOT EXISTS aggregated_score (
  timestamp         BIGINT UNSIGNED NOT NULL PRIMARY KEY,
  value             DOUBLE NULL,
  spo2_score        DOUBLE NULL,
  ecg_score         DOUBLE NULL,
  bp_score          DOUBLE NULL,
  resp_rate_score   DOUBLE NULL,
  temperature_score DOUBLE NULL
);

CREATE TABLE IF NOT EXISTS background_data (
  id                INT NOT NULL PRIMARY KEY,
  age               INT NULL,
  weight            INT NULL COMMENT 'units: kg',
  height            INT NULL COMMENT 'units: cm',
  sex               ENUM('M', 'F') NULL
);