package com.finance.accounting.service;

import com.finance.accounting.models.FinancialYear;
import com.finance.accounting.repository.FinancialYearRepository;
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
public class FinancialYearService {

    private final FinancialYearRepository financialYearRepository;
    private final TenantRepository tenantRepository;

    @Transactional
    public FinancialYear save(Long tenantId, FinancialYear financialYear) {
        if (financialYear.getId() != null) {
            return update(tenantId, financialYear.getId(), financialYear);
        }
        if (financialYearRepository.existsByTenant_IdAndLabel(tenantId, financialYear.getLabel())) {
            throw new IllegalArgumentException(
                    "Financial year label already exists: " + financialYear.getLabel());
        }
        financialYear.setTenant(tenantRepository.getReferenceById(tenantId));
        return financialYearRepository.save(financialYear);
    }

    @Transactional
    public FinancialYear update(Long tenantId, Long id, FinancialYear patch) {
        FinancialYear existing =
                financialYearRepository
                        .findByIdAndTenant_Id(id, tenantId)
                        .orElseThrow(() -> new EntityNotFoundException("Financial year not found: " + id));
        if (patch.getLabel() != null
                && !patch.getLabel().equals(existing.getLabel())
                && financialYearRepository.existsByTenant_IdAndLabelAndIdNot(
                        tenantId, patch.getLabel(), id)) {
            throw new IllegalArgumentException("Financial year label already exists: " + patch.getLabel());
        }
        if (patch.getLabel() != null) {
            existing.setLabel(patch.getLabel());
        }
        if (patch.getStartDate() != null) {
            existing.setStartDate(patch.getStartDate());
        }
        if (patch.getEndDate() != null) {
            existing.setEndDate(patch.getEndDate());
        }
        if (patch.getStatus() != null) {
            existing.setStatus(patch.getStatus());
        }
        existing.setActive(patch.isActive());
        return financialYearRepository.save(existing);
    }

    public Optional<FinancialYear> findById(Long tenantId, Long id) {
        return financialYearRepository.findByIdAndTenant_Id(id, tenantId);
    }

    public List<FinancialYear> findAll(Long tenantId) {
        return financialYearRepository.findByTenant_IdOrderByStartDateDesc(tenantId);
    }

    public Optional<FinancialYear> findActive(Long tenantId) {
        return financialYearRepository.findFirstByTenant_IdAndActiveTrueOrderByIdAsc(tenantId);
    }

    @Transactional
    public void deleteById(Long tenantId, Long id) {
        FinancialYear existing =
                financialYearRepository
                        .findByIdAndTenant_Id(id, tenantId)
                        .orElseThrow(() -> new EntityNotFoundException("Financial year not found: " + id));
        financialYearRepository.delete(existing);
    }
}
