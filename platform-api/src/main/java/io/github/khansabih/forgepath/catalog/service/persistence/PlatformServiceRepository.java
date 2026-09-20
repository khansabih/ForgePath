package io.github.khansabih.forgepath.catalog.service.persistence;

import io.github.khansabih.forgepath.catalog.service.domain.PlatformService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
@Repository
public interface PlatformServiceRepository extends JpaRepository<PlatformService, UUID> {
    boolean existsByName(String name);
    List<PlatformService> findAllByOrderByNameAsc();
    List<PlatformService> findAllByOwnerTeam_IdOrderByNameAsc(UUID ownerTeamId);
}
