package com.finance.accounting.repository;

import com.finance.accounting.models.FinancialYear;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FinancialYearRepository extends JpaRepository<FinancialYear, Long> {

    List<FinancialYear> findByTenant_IdOrderByStartDateDesc(Long tenantId);

    Optional<FinancialYear> findFirstByTenant_IdAndActiveTrueOrderByIdAsc(Long tenantId);

    Optional<FinancialYear> findByIdAndTenant_Id(Long id, Long tenantId);

    boolean existsByTenant_IdAndLabelAndIdNot(Long tenantId, String label, Long id);

    boolean existsByTenant_IdAndLabel(Long tenantId, String label);
}
