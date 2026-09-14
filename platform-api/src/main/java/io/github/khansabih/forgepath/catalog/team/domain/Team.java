package io.github.khansabih.forgepath.catalog.team.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "teams")
public class Team {
    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Team() {}

    public Team(String name){
        this.id = UUID.randomUUID();
        this.name = name;
        this.createdAt = Instant.now();
    }

    public Team(UUID id, String name, Instant createdAt) {
    }

    public UUID getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }
}
