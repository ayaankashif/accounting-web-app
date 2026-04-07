package com.finance.accounting.service;

import com.finance.accounting.models.Location;
import com.finance.accounting.repository.LocationRepository;
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
public class LocationService {

    private final LocationRepository locationRepository;
    private final TenantRepository tenantRepository;

    @Transactional
    public Location save(Long tenantId, Location location) {
        if (location.getId() != null) {
            return update(tenantId, location.getId(), location);
        }
        if (locationRepository.existsByTenant_IdAndLocationCode(tenantId, location.getLocationCode())) {
            throw new IllegalArgumentException(
                    "Location code already exists: " + location.getLocationCode());
        }
        location.setTenant(tenantRepository.getReferenceById(tenantId));
        return locationRepository.save(location);
    }

    @Transactional
    public Location update(Long tenantId, Long id, Location patch) {
        Location existing =
                locationRepository
                        .findByIdAndTenant_Id(id, tenantId)
                        .orElseThrow(() -> new EntityNotFoundException("Location not found: " + id));
        if (patch.getLocationCode() != null
                && !patch.getLocationCode().equals(existing.getLocationCode())
                && locationRepository.existsByTenant_IdAndLocationCodeAndIdNot(
                        tenantId, patch.getLocationCode(), id)) {
            throw new IllegalArgumentException("Location code already exists: " + patch.getLocationCode());
        }
        if (patch.getLocationCode() != null) {
            existing.setLocationCode(patch.getLocationCode());
        }
        if (patch.getLocationName() != null) {
            existing.setLocationName(patch.getLocationName());
        }
        existing.setActive(patch.isActive());
        return locationRepository.save(existing);
    }

    public Optional<Location> findById(Long tenantId, Long id) {
        return locationRepository.findByIdAndTenant_Id(id, tenantId);
    }

    public List<Location> findAll(Long tenantId) {
        return locationRepository.findByTenant_IdOrderByLocationCodeAsc(tenantId);
    }

    @Transactional
    public void deleteById(Long tenantId, Long id) {
        Location existing =
                locationRepository
                        .findByIdAndTenant_Id(id, tenantId)
                        .orElseThrow(() -> new EntityNotFoundException("Location not found: " + id));
        locationRepository.delete(existing);
    }
}
