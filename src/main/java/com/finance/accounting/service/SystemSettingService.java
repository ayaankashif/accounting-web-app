package com.finance.accounting.service;

import com.finance.accounting.models.SystemSetting;
import com.finance.accounting.models.SystemSettingId;
import com.finance.accounting.repository.SystemSettingRepository;
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
public class SystemSettingService {

    private final SystemSettingRepository systemSettingRepository;
    private final TenantRepository tenantRepository;

    @Transactional
    public SystemSetting save(Long tenantId, SystemSetting setting) {
        if (setting.getSettingKey() == null) {
            throw new IllegalArgumentException("settingKey is required");
        }
        if (systemSettingRepository
                .findByTenant_IdAndSettingKey(tenantId, setting.getSettingKey())
                .isPresent()) {
            throw new IllegalArgumentException("Setting already exists: " + setting.getSettingKey());
        }
        setting.setTenant(tenantRepository.getReferenceById(tenantId));
        return systemSettingRepository.save(setting);
    }

    /** Key is immutable; only the value is updated. */
    @Transactional
    public SystemSetting update(Long tenantId, String settingKey, SystemSetting patch) {
        SystemSetting existing =
                systemSettingRepository
                        .findByTenant_IdAndSettingKey(tenantId, settingKey)
                        .orElseThrow(() -> new EntityNotFoundException("Setting not found: " + settingKey));
        if (patch.getSettingValue() != null) {
            existing.setSettingValue(patch.getSettingValue());
        }
        return systemSettingRepository.save(existing);
    }

    public Optional<SystemSetting> findByKey(Long tenantId, String settingKey) {
        return systemSettingRepository.findByTenant_IdAndSettingKey(tenantId, settingKey);
    }

    public List<SystemSetting> findAll(Long tenantId) {
        return systemSettingRepository.findByTenant_Id(tenantId);
    }

    @Transactional
    public void deleteByKey(Long tenantId, String settingKey) {
        SystemSettingId id = new SystemSettingId(tenantId, settingKey);
        if (!systemSettingRepository.existsById(id)) {
            throw new EntityNotFoundException("Setting not found: " + settingKey);
        }
        systemSettingRepository.deleteById(id);
    }
}
