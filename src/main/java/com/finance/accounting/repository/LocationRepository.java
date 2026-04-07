package com.finance.accounting.repository;

import com.finance.accounting.models.Location;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, Long> {

    List<Location> findByTenant_IdOrderByLocationCodeAsc(Long tenantId);

    Optional<Location> findByIdAndTenant_Id(Long id, Long tenantId);

    boolean existsByTenant_IdAndLocationCodeAndIdNot(Long tenantId, String locationCode, Long id);

    boolean existsByTenant_IdAndLocationCode(Long tenantId, String locationCode);
}
