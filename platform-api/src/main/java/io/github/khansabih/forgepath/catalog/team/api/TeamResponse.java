package io.github.khansabih.forgepath.catalog.team.api;

import io.github.khansabih.forgepath.catalog.team.domain.Team;

import java.time.Instant;
import java.util.UUID;

public record TeamResponse(
        UUID id,
        String name,
        Instant created_at
) {
    public static TeamResponse from(Team team) {
        return new TeamResponse(
                team.getId(),
                team.getName(),
                team.getCreatedAt()
        );
    }
}
