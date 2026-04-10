package com.finance.accounting.service;

import com.finance.accounting.models.Tenant;
import com.finance.accounting.repository.TenantRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TenantService {

    private final TenantRepository tenantRepository;

    /**
     * Registers a new organization (tenant). {@code code} is normalized to lowercase with spaces as hyphens.
     */
    @Transactional
    public Tenant registerOrganization(String code, String name) {
        String normalizedCode = normalizeCode(code);
        if (!StringUtils.hasText(normalizedCode)) {
            throw new IllegalArgumentException("Organization code is required");
        }
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("Organization name is required");
        }
        if (tenantRepository.existsByCode(normalizedCode)) {
            throw new IllegalArgumentException("Organization code already exists: " + normalizedCode);
        }
        Tenant tenant = new Tenant();
        tenant.setCode(normalizedCode);
        tenant.setName(name.trim());
        tenant.setActive(true);
        return tenantRepository.save(tenant);
    }

    public static String normalizeCode(String raw) {
        if (raw == null) {
            return "";
        }
        String t = raw.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", "-");
        return t.replaceAll("[^a-z0-9._-]", "");
    }

    @Transactional
    public Tenant save(Tenant tenant) {
        return tenantRepository.save(tenant);
    }

    @Transactional
    public Tenant update(Long id, Tenant patch) {
        Tenant existing =
                tenantRepository
                        .findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Tenant not found: " + id));
        if (patch.getCode() != null
                && !patch.getCode().equals(existing.getCode())
                && tenantRepository.existsByCodeAndIdNot(patch.getCode(), id)) {
            throw new IllegalArgumentException("Tenant code already exists: " + patch.getCode());
        }
        if (patch.getCode() != null) {
            existing.setCode(patch.getCode());
        }
        if (patch.getName() != null) {
            existing.setName(patch.getName());
        }
        existing.setActive(patch.isActive());
        return tenantRepository.save(existing);
    }

    public Optional<Tenant> findById(Long id) {
        return tenantRepository.findById(id);
    }

    public Optional<Tenant> findByCode(String code) {
        return tenantRepository.findByCode(code);
    }

    public List<Tenant> findAll() {
        return tenantRepository.findAll();
    }

    public List<Tenant> findAllActive() {
        return tenantRepository.findByActiveTrueOrderByNameAsc();
    }

    @Transactional
    public void deleteById(Long id) {
        tenantRepository.deleteById(id);
    }
}
