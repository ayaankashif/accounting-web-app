package com.finance.accounting.repository;

import com.finance.accounting.models.AppModule;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppModuleRepository extends JpaRepository<AppModule, Long> {

    Optional<AppModule> findByModuleCode(String moduleCode);

    boolean existsByModuleCodeAndIdNot(String moduleCode, Long id);
}
