package io.github.khansabih.forgepath.catalog.service.api;

import io.github.khansabih.forgepath.catalog.service.domain.PlatformService;
import io.github.khansabih.forgepath.catalog.service.domain.ServiceFramework;
import io.github.khansabih.forgepath.catalog.service.domain.ServiceRuntime;

import java.time.Instant;
import java.util.UUID;

public record PlatformServiceResponse(
        UUID id,
        String name,
        String description,
        UUID ownerTeamId,
        String ownerTeamName,
        ServiceFramework framework,
        ServiceRuntime runtime,
        Instant createdAt,
        Instant updatedAt
) {
    public static PlatformServiceResponse from(PlatformService platformService) {
        return new PlatformServiceResponse(
                platformService.getId(),
                platformService.getName(),
                platformService.getDescription(),
                platformService.getOwnerTeam().getId(),
                platformService.getOwnerTeam().getName(),
                platformService.getFramework(),
                platformService.getRuntime(),
                platformService.getCreatedAt(),
                platformService.getUpdatedAt()
        );
    }
}
