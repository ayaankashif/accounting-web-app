package com.finance.accounting.service;

import com.finance.accounting.models.AppModule;
import com.finance.accounting.models.AppRole;
import com.finance.accounting.models.RolePrivilege;
import com.finance.accounting.repository.AppModuleRepository;
import com.finance.accounting.repository.AppRoleRepository;
import com.finance.accounting.repository.RolePrivilegeRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RolePrivilegeService {

    private final RolePrivilegeRepository rolePrivilegeRepository;
    private final AppRoleRepository appRoleRepository;
    private final AppModuleRepository appModuleRepository;

    @Transactional
    public RolePrivilege save(Long tenantId, RolePrivilege privilege) {
        if (privilege.getRole() == null || privilege.getRole().getId() == null) {
            throw new IllegalArgumentException("Role with id is required");
        }
        if (privilege.getModule() == null || privilege.getModule().getId() == null) {
            throw new IllegalArgumentException("Module with id is required");
        }
        AppRole role =
                appRoleRepository
                        .findByIdAndTenant_Id(privilege.getRole().getId(), tenantId)
                        .orElseThrow(
                                () ->
                                        new EntityNotFoundException(
                                                "Role not found: " + privilege.getRole().getId()));
        privilege.setRole(role);
        AppModule module =
                appModuleRepository
                        .findById(privilege.getModule().getId())
                        .orElseThrow(
                                () ->
                                        new EntityNotFoundException(
                                                "Module not found: " + privilege.getModule().getId()));
        privilege.setModule(module);
        return rolePrivilegeRepository.save(privilege);
    }

    @Transactional
    public RolePrivilege update(Long tenantId, Long id, RolePrivilege patch) {
        RolePrivilege existing =
                rolePrivilegeRepository
                        .findByIdAndRole_Tenant_Id(id, tenantId)
                        .orElseThrow(() -> new EntityNotFoundException("Role privilege not found: " + id));
        if (patch.getRole() != null && patch.getRole().getId() != null) {
            AppRole role =
                    appRoleRepository
                            .findByIdAndTenant_Id(patch.getRole().getId(), tenantId)
                            .orElseThrow(
                                    () ->
                                            new EntityNotFoundException(
                                                    "Role not found: " + patch.getRole().getId()));
            existing.setRole(role);
        }
        if (patch.getModule() != null && patch.getModule().getId() != null) {
            AppModule module =
                    appModuleRepository
                            .findById(patch.getModule().getId())
                            .orElseThrow(
                                    () ->
                                            new EntityNotFoundException(
                                                    "Module not found: " + patch.getModule().getId()));
            existing.setModule(module);
        }
        existing.setCanView(patch.isCanView());
        existing.setCanInsert(patch.isCanInsert());
        existing.setCanUpdate(patch.isCanUpdate());
        existing.setCanDelete(patch.isCanDelete());
        return rolePrivilegeRepository.save(existing);
    }

    public Optional<RolePrivilege> findById(Long tenantId, Long id) {
        return rolePrivilegeRepository.findByIdAndRole_Tenant_Id(id, tenantId);
    }

    public Optional<RolePrivilege> findByRoleIdAndModuleId(Long tenantId, Long roleId, Long moduleId) {
        appRoleRepository
                .findByIdAndTenant_Id(roleId, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found: " + roleId));
        return rolePrivilegeRepository.findByRole_IdAndModule_Id(roleId, moduleId);
    }

    public List<RolePrivilege> findByRoleId(Long tenantId, Long roleId) {
        appRoleRepository
                .findByIdAndTenant_Id(roleId, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found: " + roleId));
        return rolePrivilegeRepository.findByRole_Tenant_IdAndRole_Id(tenantId, roleId);
    }

    public List<RolePrivilege> findAll() {
        return rolePrivilegeRepository.findAll();
    }

    @Transactional
    public void deleteById(Long tenantId, Long id) {
        RolePrivilege existing =
                rolePrivilegeRepository
                        .findByIdAndRole_Tenant_Id(id, tenantId)
                        .orElseThrow(() -> new EntityNotFoundException("Role privilege not found: " + id));
        rolePrivilegeRepository.delete(existing);
    }

    @Transactional
    public void deleteByRoleId(Long tenantId, Long roleId) {
        appRoleRepository
                .findByIdAndTenant_Id(roleId, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found: " + roleId));
        rolePrivilegeRepository.deleteByRole_Id(roleId);
    }
}
