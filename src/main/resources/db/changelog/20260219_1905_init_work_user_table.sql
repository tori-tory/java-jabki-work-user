CREATE SCHEMA IF NOT EXISTS work_user;

CREATE TABLE IF NOT EXISTS work_user."user" (
    id SERIAL PRIMARY KEY,
    username VARCHAR NOT NULL,
    password VARCHAR(100) NOT NULL,
	created_at timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at timestamp without time zone,
    UNIQUE (username)
);