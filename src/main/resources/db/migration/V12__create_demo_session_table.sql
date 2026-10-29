CREATE TABLE demo_session(
id BIGINT PRIMARY KEY AUTO_INCREMENT,
user_id BIGINT UNIQUE NOT NULL,
store_id BIGINT UNIQUE NOT NULL,
expires_at DATETIME(6) NOT NULL,
status VARCHAR(50) NOT NULL,
created_at DATETIME(6) NOT NULL
);

CREATE INDEX ind_demo_session_status_expires ON demo_session(status,expires_at);