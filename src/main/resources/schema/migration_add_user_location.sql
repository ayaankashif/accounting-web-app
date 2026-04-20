-- DEPRECATED: the app now uses `users.location_id` → `locations` (see migration_users_primary_location_fk.sql).
-- This script only for legacy DBs that need the old free-text column before migration.
-- Add free-text "User location" (branch/office) on users. Master data locations
-- stay in `locations` + `user_locations`; this column is not a foreign key.
ALTER TABLE users
    ADD COLUMN user_location VARCHAR(255) NULL COMMENT 'free-text branch/office (not FK to locations)'
    AFTER email;
