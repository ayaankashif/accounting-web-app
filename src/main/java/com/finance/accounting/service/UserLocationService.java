package com.finance.accounting.service;

import com.finance.accounting.models.Location;
import com.finance.accounting.models.User;
import com.finance.accounting.models.UserLocation;
import com.finance.accounting.repository.LocationRepository;
import com.finance.accounting.repository.UserLocationRepository;
import com.finance.accounting.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserLocationService {

    private final UserLocationRepository userLocationRepository;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;

    @Transactional
    public UserLocation save(Long tenantId, UserLocation userLocation) {
        if (userLocation.getUser() == null || userLocation.getUser().getId() == null) {
            throw new IllegalArgumentException("User with id is required");
        }
        if (userLocation.getLocation() == null || userLocation.getLocation().getId() == null) {
            throw new IllegalArgumentException("Location with id is required");
        }
        User user =
                userRepository
                        .findByIdAndTenant_Id(userLocation.getUser().getId(), tenantId)
                        .orElseThrow(
                                () ->
                                        new EntityNotFoundException(
                                                "User not found: " + userLocation.getUser().getId()));
        Location location =
                locationRepository
                        .findByIdAndTenant_Id(userLocation.getLocation().getId(), tenantId)
                        .orElseThrow(
                                () ->
                                        new EntityNotFoundException(
                                                "Location not found: " + userLocation.getLocation().getId()));
        userLocation.setUser(user);
        userLocation.setLocation(location);
        return userLocationRepository.save(userLocation);
    }

    @Transactional
    public UserLocation update(Long tenantId, Long id, UserLocation patch) {
        UserLocation existing =
                userLocationRepository
                        .findByIdAndUser_Tenant_Id(id, tenantId)
                        .orElseThrow(() -> new EntityNotFoundException("User location not found: " + id));
        if (patch.getUser() != null && patch.getUser().getId() != null) {
            User user =
                    userRepository
                            .findByIdAndTenant_Id(patch.getUser().getId(), tenantId)
                            .orElseThrow(
                                    () ->
                                            new EntityNotFoundException(
                                                    "User not found: " + patch.getUser().getId()));
            existing.setUser(user);
        }
        if (patch.getLocation() != null && patch.getLocation().getId() != null) {
            Location location =
                    locationRepository
                            .findByIdAndTenant_Id(patch.getLocation().getId(), tenantId)
                            .orElseThrow(
                                    () ->
                                            new EntityNotFoundException(
                                                    "Location not found: " + patch.getLocation().getId()));
            existing.setLocation(location);
        }
        if (patch.getAssignedAt() != null) {
            existing.setAssignedAt(patch.getAssignedAt());
        }
        return userLocationRepository.save(existing);
    }

    public Optional<UserLocation> findById(Long tenantId, Long id) {
        return userLocationRepository.findByIdAndUser_Tenant_Id(id, tenantId);
    }

    public List<UserLocation> findByUserId(Long tenantId, Long userId) {
        userRepository
                .findByIdAndTenant_Id(userId, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        return userLocationRepository.findByUser_Id(userId);
    }

    public List<UserLocation> findAll() {
        return userLocationRepository.findAll();
    }

    @Transactional
    public void deleteById(Long tenantId, Long id) {
        UserLocation existing =
                userLocationRepository
                        .findByIdAndUser_Tenant_Id(id, tenantId)
                        .orElseThrow(() -> new EntityNotFoundException("User location not found: " + id));
        userLocationRepository.delete(existing);
    }

    /**
     * Replaces all location assignments for a user (matches "User Location" save behaviour).
     */
    @Transactional
    public List<UserLocation> replaceLocationsForUser(Long tenantId, Long userId, List<Long> locationIds) {
        User user =
                userRepository
                        .findByIdAndTenant_Id(userId, tenantId)
                        .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        userLocationRepository.deleteByUser_Id(userId);

        if (locationIds == null || locationIds.isEmpty()) {
            return List.of();
        }

        LocalDateTime now = LocalDateTime.now();
        List<UserLocation> saved = new ArrayList<>();
        for (Long locationId : locationIds) {
            Location location =
                    locationRepository
                            .findByIdAndTenant_Id(locationId, tenantId)
                            .orElseThrow(() -> new IllegalArgumentException("Location not found: " + locationId));
            UserLocation ul = new UserLocation();
            ul.setUser(user);
            ul.setLocation(location);
            ul.setAssignedAt(now);
            saved.add(userLocationRepository.save(ul));
        }
        return saved;
    }
}
