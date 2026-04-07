package com.finance.accounting.service;

import com.finance.accounting.models.AppRole;
import com.finance.accounting.repository.AppRoleRepository;
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
public class AppRoleService {

    private final AppRoleRepository appRoleRepository;
    private final TenantRepository tenantRepository;

    @Transactional
    public AppRole save(Long tenantId, AppRole role) {
        if (role.getId() != null) {
            return update(tenantId, role.getId(), role);
        }
        if (appRoleRepository.existsByTenant_IdAndDescription(tenantId, role.getDescription())) {
            throw new IllegalArgumentException(
                    "Role description already exists: " + role.getDescription());
        }
        role.setTenant(tenantRepository.getReferenceById(tenantId));
        return appRoleRepository.save(role);
    }

    @Transactional
    public AppRole update(Long tenantId, Long id, AppRole patch) {
        AppRole existing =
                appRoleRepository
                        .findByIdAndTenant_Id(id, tenantId)
                        .orElseThrow(() -> new EntityNotFoundException("Role not found: " + id));
        if (patch.getDescription() != null
                && !patch.getDescription().equals(existing.getDescription())
                && appRoleRepository.existsByTenant_IdAndDescriptionAndIdNot(
                        tenantId, patch.getDescription(), id)) {
            throw new IllegalArgumentException("Role description already exists: " + patch.getDescription());
        }
        if (patch.getRoleCode() != null) {
            existing.setRoleCode(patch.getRoleCode());
        }
        if (patch.getDescription() != null) {
            existing.setDescription(patch.getDescription());
        }
        if (patch.getRemarks() != null) {
            existing.setRemarks(patch.getRemarks());
        }
        return appRoleRepository.save(existing);
    }

    public Optional<AppRole> findById(Long tenantId, Long id) {
        return appRoleRepository.findByIdAndTenant_Id(id, tenantId);
    }

    public List<AppRole> findAll(Long tenantId) {
        return appRoleRepository.findByTenant_IdOrderByDescriptionAsc(tenantId);
    }

    @Transactional
    public void deleteById(Long tenantId, Long id) {
        AppRole existing =
                appRoleRepository
                        .findByIdAndTenant_Id(id, tenantId)
                        .orElseThrow(() -> new EntityNotFoundException("Role not found: " + id));
        appRoleRepository.delete(existing);
    }
}
