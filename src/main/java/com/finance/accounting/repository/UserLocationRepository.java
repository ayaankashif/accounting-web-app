package com.finance.accounting.repository;

import com.finance.accounting.models.UserLocation;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserLocationRepository extends JpaRepository<UserLocation, Long> {

    List<UserLocation> findByUser_Id(Long userId);

    void deleteByUser_Id(Long userId);

    Optional<UserLocation> findByIdAndUser_Tenant_Id(Long id, Long tenantId);
}
