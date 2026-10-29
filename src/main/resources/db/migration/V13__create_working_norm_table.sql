CREATE TABLE working_norm(
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    year_number INT NOT NULL,
    month_number INT NOT NULL,
    hours INT NOT NULL,
    created_at DATETIME NOT NULL,
    created_by_user_id BIGINT NULL,
    created_by_label VARCHAR(255) NOT NULL,
    updated_at DATETIME,
    updated_by_user_id BIGINT,
    updated_by_label VARCHAR(255) NULL,
    CONSTRAINT fk_working_norm_created_by FOREIGN KEY (created_by_user_id) REFERENCES app_user (id) ON DELETE SET NULL,
    CONSTRAINT fk_working_norm_updated_by FOREIGN KEY (updated_by_user_id) REFERENCES app_user (id) ON DELETE SET NULL,
    CONSTRAINT uq_working_norm UNIQUE (year_number, month_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;