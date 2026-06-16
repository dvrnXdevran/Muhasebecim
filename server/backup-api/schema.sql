CREATE TABLE IF NOT EXISTS muhasebecim_backups (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    device_id VARCHAR(128) NOT NULL,
    created_at BIGINT NOT NULL,
    app_version VARCHAR(64) NOT NULL,
    database_version INT NOT NULL,
    payload_json LONGTEXT NOT NULL,
    inserted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_device_created (device_id, created_at),
    INDEX idx_device_id (device_id, id)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

