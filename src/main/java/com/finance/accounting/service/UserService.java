package com.finance.accounting.service;

import com.finance.accounting.models.AppRole;
import com.finance.accounting.models.User;
import com.finance.accounting.repository.AppRoleRepository;
import com.finance.accounting.repository.TenantRepository;
import com.finance.accounting.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final AppRoleRepository appRoleRepository;
    private final TenantRepository tenantRepository;

    @Transactional
    public User save(Long tenantId, User user) {
        if (user.getId() != null) {
            return update(tenantId, user.getId(), user);
        }
        if (userRepository.existsByTenant_IdAndUsername(tenantId, user.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + user.getUsername());
        }
        if (user.getUserCode() != null
                && !user.getUserCode().isEmpty()
                && userRepository.existsByTenant_IdAndUserCode(tenantId, user.getUserCode())) {
            throw new IllegalArgumentException("User code already exists: " + user.getUserCode());
        }
        user.setTenant(tenantRepository.getReferenceById(tenantId));
        AppRole role = resolveRoleForTenant(tenantId, user.getRole());
        user.setRole(role);
        return userRepository.save(user);
    }

    @Transactional
    public User update(Long tenantId, Long id, User patch) {
        User existing =
                userRepository
                        .findByIdAndTenant_Id(id, tenantId)
                        .orElseThrow(() -> new EntityNotFoundException("User not found: " + id));
        if (patch.getUsername() != null
                && !patch.getUsername().equals(existing.getUsername())
                && userRepository.existsByTenant_IdAndUsernameAndIdNot(tenantId, patch.getUsername(), id)) {
            throw new IllegalArgumentException("Username already exists: " + patch.getUsername());
        }
        if (patch.getUserCode() != null
                && !patch.getUserCode().isEmpty()
                && !patch.getUserCode().equals(existing.getUserCode())
                && userRepository.existsByTenant_IdAndUserCodeAndIdNot(tenantId, patch.getUserCode(), id)) {
            throw new IllegalArgumentException("User code already exists: " + patch.getUserCode());
        }
        if (patch.getUsername() != null) {
            existing.setUsername(patch.getUsername());
        }
        existing.setUserCode(patch.getUserCode());
        if (StringUtils.hasText(patch.getPasswordHash())) {
            existing.setPasswordHash(patch.getPasswordHash());
        }
        if (patch.getFullName() != null) {
            existing.setFullName(patch.getFullName());
        }
        existing.setJobDescription(patch.getJobDescription());
        existing.setEmail(patch.getEmail());
        existing.setExpiryDate(patch.getExpiryDate());
        existing.setAmountLimit(patch.getAmountLimit());
        existing.setSignatureFilePath(patch.getSignatureFilePath());
        existing.setVoucherReversalAllowed(patch.isVoucherReversalAllowed());
        existing.setPostedMaintenanceEditAllowed(patch.isPostedMaintenanceEditAllowed());
        existing.setAllowOldRateInBooking(patch.isAllowOldRateInBooking());
        existing.setActive(patch.isActive());
        if (patch.getRole() != null && patch.getRole().getId() != null) {
            existing.setRole(resolveRoleForTenant(tenantId, patch.getRole()));
        }
        return userRepository.save(existing);
    }

    private AppRole resolveRoleForTenant(Long tenantId, AppRole roleRef) {
        if (roleRef == null || roleRef.getId() == null) {
            throw new IllegalArgumentException("Role is required");
        }
        return appRoleRepository
                .findByIdAndTenant_Id(roleRef.getId(), tenantId)
                .orElseThrow(
                        () -> new EntityNotFoundException("Role not found: " + roleRef.getId()));
    }

    public Optional<User> findById(Long tenantId, Long id) {
        return userRepository.findByIdAndTenant_Id(id, tenantId);
    }

    public Optional<User> findByUsername(Long tenantId, String username) {
        return userRepository.findByTenant_IdAndUsername(tenantId, username);
    }

    public List<User> findAll(Long tenantId) {
        return userRepository.findByTenant_IdOrderByUsernameAsc(tenantId);
    }

    @Transactional
    public void deleteById(Long tenantId, Long id) {
        User existing =
                userRepository
                        .findByIdAndTenant_Id(id, tenantId)
                        .orElseThrow(() -> new EntityNotFoundException("User not found: " + id));
        userRepository.delete(existing);
    }

    public boolean existsByUsername(Long tenantId, String username) {
        return userRepository.existsByTenant_IdAndUsername(tenantId, username);
    }
}
