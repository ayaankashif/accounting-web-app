package com.finance.accounting.repository;

import com.finance.accounting.models.RolePrivilege;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolePrivilegeRepository extends JpaRepository<RolePrivilege, Long> {

    List<RolePrivilege> findByRole_Id(Long roleId);

    List<RolePrivilege> findByRole_Tenant_IdAndRole_Id(Long tenantId, Long roleId);

    Optional<RolePrivilege> findByRole_IdAndModule_Id(Long roleId, Long moduleId);

    Optional<RolePrivilege> findByIdAndRole_Tenant_Id(Long id, Long tenantId);

    void deleteByRole_Id(Long roleId);
}
