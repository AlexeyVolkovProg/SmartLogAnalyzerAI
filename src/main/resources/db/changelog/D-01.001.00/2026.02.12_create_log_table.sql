--liquibase formatted sql

--changeset alexey:1 labels:init context:all
--comment: Create schema log_common
CREATE SCHEMA IF NOT EXISTS log_common;

--changeset alexey:2 labels:init context:all
--comment: Enable pgvector extension
CREATE EXTENSION IF NOT EXISTS vector;

--changeset alexey:3 labels:init context:all
--comment: Create log_entries table
CREATE TABLE log_common.log_entries (
                                        id           BIGSERIAL      PRIMARY KEY,
                                        timestamp    TIMESTAMPTZ    NOT NULL,
                                        service_name VARCHAR(100)   NOT NULL,
                                        log_level    VARCHAR(10)    NOT NULL,
                                        message      TEXT           NOT NULL,
                                        embedding    vector(1024)
);

--changeset alexey:4 labels:init context:all
--comment: Index for filtering by service and time
CREATE INDEX idx_log_entries_service_time ON log_common.log_entries (service_name, timestamp);

--changeset alexey:5 labels:init context:all
--comment: Index for filtering by log level
CREATE INDEX idx_log_entries_level ON log_common.log_entries (log_level);