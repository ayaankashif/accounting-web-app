package com.finance.accounting.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "roles",
        uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "description"}))
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AppRole extends TenantScopedModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role_code", length = 16)
    private String roleCode;

    @Column(nullable = false)
    private String description;

    @Column(columnDefinition = "TEXT")
    private String remarks;
}
