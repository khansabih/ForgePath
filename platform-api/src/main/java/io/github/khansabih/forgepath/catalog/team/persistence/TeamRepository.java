package io.github.khansabih.forgepath.catalog.team.persistence;


import io.github.khansabih.forgepath.catalog.team.domain.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TeamRepository extends JpaRepository<Team, UUID> {
    boolean existsByName(String name);
    List<Team> findAllByOrderByNameAsc();
}
