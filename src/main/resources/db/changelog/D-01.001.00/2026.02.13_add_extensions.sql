--liquibase formatted sql

--changeset alexey:1
--comment: create vector extension
CREATE EXTENSION IF NOT EXISTS vector SCHEMA log_common;

--changeset alexey:2
--comment: create uuid extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp" SCHEMA log_common;

--changeset alexey:3
--comment: add grants
GRANT ALL ON SCHEMA log_common TO postgres;
GRANT ALL ON ALL TABLES IN SCHEMA log_common TO postgres;