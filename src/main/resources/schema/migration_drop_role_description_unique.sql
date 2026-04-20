-- Run once if the database was created with uk_roles_tenant_description.
-- Required to allow multiple roles with the same description per tenant.
ALTER TABLE roles DROP INDEX uk_roles_tenant_description;
