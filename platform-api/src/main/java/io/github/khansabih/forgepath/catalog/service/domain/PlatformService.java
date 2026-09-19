package io.github.khansabih.forgepath.catalog.service.domain;

import io.github.khansabih.forgepath.catalog.team.domain.Team;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "services")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlatformService {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_team_id", nullable = false)
    private Team ownerTeam;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ServiceFramework framework;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ServiceRuntime runtime;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public PlatformService(
            String name,
            String description,
            Team ownerTeam,
            ServiceFramework framework,
            ServiceRuntime runtime
    ){
        this.id = UUID.randomUUID();
        this.name = name;
        this.description = description;
        this.ownerTeam = ownerTeam;
        this.framework = framework;
        this.runtime = runtime;

        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }
}
