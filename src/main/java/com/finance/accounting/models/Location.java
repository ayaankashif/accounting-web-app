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
        name = "locations",
        uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "location_code"}))
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Location extends TenantScopedModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "location_code", nullable = false, length = 32)
    private String locationCode;

    @Column(name = "location_name", nullable = false)
    private String locationName;

    @Column(nullable = false)
    private boolean active = true;
}
