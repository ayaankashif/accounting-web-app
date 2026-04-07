package com.finance.accounting.service;

import com.finance.accounting.models.Tenant;
import com.finance.accounting.repository.TenantRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TenantService {

    private final TenantRepository tenantRepository;

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

    @Transactional
    public void deleteById(Long id) {
        tenantRepository.deleteById(id);
    }
}
