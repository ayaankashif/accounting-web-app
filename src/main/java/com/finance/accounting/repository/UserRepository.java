package com.finance.accounting.repository;

import com.finance.accounting.models.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findByTenant_IdOrderByUsernameAsc(Long tenantId);

    Optional<User> findByIdAndTenant_Id(Long id, Long tenantId);

    Optional<User> findByTenant_IdAndUsername(Long tenantId, String username);

    boolean existsByTenant_IdAndUsernameAndIdNot(Long tenantId, String username, Long id);

    boolean existsByTenant_IdAndUserCodeAndIdNot(Long tenantId, String userCode, Long id);

    boolean existsByTenant_IdAndUsername(Long tenantId, String username);

    boolean existsByTenant_IdAndUserCode(Long tenantId, String userCode);
}
