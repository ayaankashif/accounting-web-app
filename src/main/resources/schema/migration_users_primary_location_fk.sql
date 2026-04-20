-- Optional user.default location: reference to locations master.
-- Replaces free-text `user_location` (if you ran migration_add_user_location.sql).

-- For existing DBs: add column + FK, then drop legacy text.
ALTER TABLE users
    ADD COLUMN location_id BIGINT UNSIGNED NULL
    COMMENT 'optional default site; FK to locations'
    AFTER email;

ALTER TABLE users
    ADD KEY ix_users_location (location_id);

ALTER TABLE users
    ADD CONSTRAINT fk_users_location
    FOREIGN KEY (location_id) REFERENCES locations (id) ON DELETE SET NULL;

-- If this fails with "Unknown column 'user_location'", the column was never created — continue.
-- ALTER TABLE users DROP COLUMN user_location;
