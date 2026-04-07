package com.finance.accounting.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"tenant_id", "username"}),
                @UniqueConstraint(columnNames = {"tenant_id", "user_code"})
        })
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class User extends TenantScopedModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_code", length = 32)
    private String userCode;

    @Column(nullable = false, length = 128)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "job_description", length = 512)
    private String jobDescription;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private AppRole role;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "amount_limit", precision = 19, scale = 4)
    private BigDecimal amountLimit;

    @Column(name = "voucher_reversal_allowed", nullable = false)
    private boolean voucherReversalAllowed;

    @Column(name = "posted_maintenance_edit_allowed", nullable = false)
    private boolean postedMaintenanceEditAllowed;

    @Column(name = "allow_old_rate_in_booking", nullable = false)
    private boolean allowOldRateInBooking;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "signature_file_path", length = 512)
    private String signatureFilePath;

    @Column(length = 255)
    private String email;
}
