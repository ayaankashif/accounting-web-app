package com.finance.accounting.service;

import com.finance.accounting.models.AppModule;
import com.finance.accounting.repository.AppModuleRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppModuleService {

    private final AppModuleRepository appModuleRepository;

    @Transactional
    public AppModule save(AppModule module) {
        return appModuleRepository.save(module);
    }

    @Transactional
    public AppModule update(Long id, AppModule patch) {
        AppModule existing =
                appModuleRepository
                        .findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Module not found: " + id));
        if (patch.getModuleCode() != null
                && !patch.getModuleCode().equals(existing.getModuleCode())
                && appModuleRepository.existsByModuleCodeAndIdNot(patch.getModuleCode(), id)) {
            throw new IllegalArgumentException("Module code already exists: " + patch.getModuleCode());
        }
        if (patch.getModuleCode() != null) {
            existing.setModuleCode(patch.getModuleCode());
        }
        if (patch.getMenuPath() != null) {
            existing.setMenuPath(patch.getMenuPath());
        }
        if (patch.getFormName() != null) {
            existing.setFormName(patch.getFormName());
        }
        existing.setSortOrder(patch.getSortOrder());
        return appModuleRepository.save(existing);
    }

    public Optional<AppModule> findById(Long id) {
        return appModuleRepository.findById(id);
    }

    public Optional<AppModule> findByModuleCode(String moduleCode) {
        return appModuleRepository.findByModuleCode(moduleCode);
    }

    public List<AppModule> findAll() {
        return appModuleRepository.findAll();
    }

    @Transactional
    public void deleteById(Long id) {
        appModuleRepository.deleteById(id);
    }
}