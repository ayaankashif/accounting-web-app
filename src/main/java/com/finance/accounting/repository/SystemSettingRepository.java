package com.finance.accounting.repository;

import com.finance.accounting.models.SystemSetting;
import com.finance.accounting.models.SystemSettingId;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemSettingRepository extends JpaRepository<SystemSetting, SystemSettingId> {

    List<SystemSetting> findByTenant_Id(Long tenantId);

    Optional<SystemSetting> findByTenant_IdAndSettingKey(Long tenantId, String settingKey);
}
