package com.finance.accounting.repository;

import com.finance.accounting.models.AppRole;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppRoleRepository extends JpaRepository<AppRole, Long> {

    List<AppRole> findByTenant_IdOrderByDescriptionAsc(Long tenantId);

    Optional<AppRole> findByIdAndTenant_Id(Long id, Long tenantId);
}
