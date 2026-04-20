package com.finance.accounting.repository;

import com.finance.accounting.models.UserLocation;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserLocationRepository extends JpaRepository<UserLocation, Long> {

    @EntityGraph(attributePaths = {"location", "user"})
    @Query("select ul from UserLocation ul where ul.user.id = :userId")
    List<UserLocation> findByUserIdWithLocationGraph(@Param("userId") Long userId);

    @EntityGraph(attributePaths = {"user", "location"})
    @Query(
            "select ul from UserLocation ul where ul.user.tenant.id = :tenantId "
                    + "order by ul.user.username asc, ul.location.locationCode asc")
    List<UserLocation> findByUser_Tenant_IdOrderByUser_UsernameAscLocation_LocationCodeAsc(
            @Param("tenantId") Long tenantId);

    void deleteByUser_Id(Long userId);

    Optional<UserLocation> findByIdAndUser_Tenant_Id(Long id, Long tenantId);
}
