package com.finance.accounting.repository;

import com.finance.accounting.models.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    @EntityGraph(attributePaths = {"location", "role"})
    @Query("select u from User u where u.tenant.id = :tenantId order by u.username asc")
    List<User> findByTenant_IdOrderByUsernameAsc(@Param("tenantId") Long tenantId);

    Optional<User> findByIdAndTenant_Id(Long id, Long tenantId);

    Optional<User> findByTenant_IdAndUsername(Long tenantId, String username);

    boolean existsByTenant_IdAndUsernameAndIdNot(Long tenantId, String username, Long id);

    boolean existsByTenant_IdAndUserCodeAndIdNot(Long tenantId, String userCode, Long id);

    boolean existsByTenant_IdAndUsername(Long tenantId, String username);

    boolean existsByTenant_IdAndUserCode(Long tenantId, String userCode);
}
