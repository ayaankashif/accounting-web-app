-- CMFS administrator module — MySQL 8+ DDL (multi-tenant: shared database, row-level isolation)
-- app_modules / role_privileges reference global catalog modules; roles are per-tenant.

CREATE TABLE IF NOT EXISTS tenants (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    code            VARCHAR(64) NOT NULL COMMENT 'stable slug, e.g. subdomain',
    name            VARCHAR(255) NOT NULL,
    active          BIT(1) NOT NULL DEFAULT 1,
    created_at      DATETIME(6) NULL,
    modified_at     DATETIME(6) NULL,
    UNIQUE KEY uk_tenants_code (code),
    KEY ix_tenants_active (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS financial_years (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    tenant_id       BIGINT UNSIGNED NOT NULL,
    label           VARCHAR(32) NOT NULL COMMENT 'e.g. 2015-2016',
    start_date      DATE NOT NULL,
    end_date        DATE NOT NULL,
    status          VARCHAR(16) NOT NULL DEFAULT 'OPEN' COMMENT 'OPEN, CLOSED',
    active          BIT(1) NOT NULL DEFAULT 0 COMMENT 'one active FY per tenant for home screen',
    created_at      DATETIME(6) NULL,
    modified_at     DATETIME(6) NULL,
    UNIQUE KEY uk_financial_years_tenant_label (tenant_id, label),
    KEY ix_financial_years_tenant (tenant_id),
    CONSTRAINT fk_financial_years_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS system_settings (
    tenant_id       BIGINT UNSIGNED NOT NULL,
    setting_key     VARCHAR(64) NOT NULL,
    setting_value   VARCHAR(512) NULL,
    created_at      DATETIME(6) NULL,
    modified_at     DATETIME(6) NULL,
    PRIMARY KEY (tenant_id, setting_key),
    CONSTRAINT fk_system_settings_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS locations (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    tenant_id       BIGINT UNSIGNED NOT NULL,
    location_code   VARCHAR(32) NOT NULL COMMENT 'e.g. 000001',
    location_name   VARCHAR(255) NOT NULL,
    active          BIT(1) NOT NULL DEFAULT 1,
    created_at      DATETIME(6) NULL,
    modified_at     DATETIME(6) NULL,
    UNIQUE KEY uk_locations_tenant_code (tenant_id, location_code),
    KEY ix_locations_tenant (tenant_id),
    CONSTRAINT fk_locations_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS roles (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    tenant_id       BIGINT UNSIGNED NOT NULL,
    role_code       VARCHAR(16) NULL COMMENT 'display id e.g. 001',
    description     VARCHAR(255) NOT NULL COMMENT 'BOOKING, Manager, Administrator…',
    remarks         TEXT NULL,
    created_at      DATETIME(6) NULL,
    modified_at     DATETIME(6) NULL,
    KEY ix_roles_tenant (tenant_id),
    KEY ix_roles_tenant_description (tenant_id, description),
    CONSTRAINT fk_roles_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS users (
    id                                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    tenant_id                           BIGINT UNSIGNED NOT NULL,
    user_code                           VARCHAR(32) NULL COMMENT 'short code e.g. 007',
    username                            VARCHAR(128) NOT NULL,
    password_hash                       VARCHAR(255) NOT NULL,
    full_name                           VARCHAR(255) NOT NULL,
    job_description                     VARCHAR(512) NULL,
    role_id                             BIGINT UNSIGNED NOT NULL,
    expiry_date                         DATE NULL,
    amount_limit                        DECIMAL(19,4) NULL,
    voucher_reversal_allowed            BIT(1) NOT NULL DEFAULT 0,
    posted_maintenance_edit_allowed     BIT(1) NOT NULL DEFAULT 0,
    allow_old_rate_in_booking           BIT(1) NOT NULL DEFAULT 0,
    active                              BIT(1) NOT NULL DEFAULT 1,
    signature_file_path                 VARCHAR(512) NULL,
    email                               VARCHAR(255) NULL,
    location_id                         BIGINT UNSIGNED NULL COMMENT 'optional default site; FK to locations',
    created_at                          DATETIME(6) NULL,
    modified_at                         DATETIME(6) NULL,
    UNIQUE KEY uk_users_tenant_username (tenant_id, username),
    UNIQUE KEY uk_users_tenant_user_code (tenant_id, user_code),
    KEY ix_users_tenant (tenant_id),
    KEY ix_users_location (location_id),
    CONSTRAINT fk_users_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles (id),
    CONSTRAINT fk_users_location FOREIGN KEY (location_id) REFERENCES locations (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS user_locations (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT UNSIGNED NOT NULL,
    location_id     BIGINT UNSIGNED NOT NULL,
    assigned_at     DATETIME(6) NULL,
    created_at      DATETIME(6) NULL,
    modified_at     DATETIME(6) NULL,
    UNIQUE KEY uk_user_locations (user_id, location_id),
    CONSTRAINT fk_user_locations_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_locations_location FOREIGN KEY (location_id) REFERENCES locations (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS app_modules (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    module_code     VARCHAR(128) NOT NULL COMMENT 'stable key for security rules',
    menu_path       VARCHAR(512) NOT NULL COMMENT 'e.g. Setup >> Project >> Project',
    form_name       VARCHAR(255) NOT NULL,
    sort_order      INT NOT NULL DEFAULT 0,
    created_at      DATETIME(6) NULL,
    modified_at     DATETIME(6) NULL,
    UNIQUE KEY uk_app_modules_code (module_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS role_privileges (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    role_id         BIGINT UNSIGNED NOT NULL,
    module_id       BIGINT UNSIGNED NOT NULL,
    can_view        BIT(1) NOT NULL DEFAULT 0,
    can_insert      BIT(1) NOT NULL DEFAULT 0,
    can_update      BIT(1) NOT NULL DEFAULT 0,
    can_delete      BIT(1) NOT NULL DEFAULT 0,
    created_at      DATETIME(6) NULL,
    modified_at     DATETIME(6) NULL,
    UNIQUE KEY uk_role_privileges (role_id, module_id),
    CONSTRAINT fk_role_privileges_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE,
    CONSTRAINT fk_role_privileges_module FOREIGN KEY (module_id) REFERENCES app_modules (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
