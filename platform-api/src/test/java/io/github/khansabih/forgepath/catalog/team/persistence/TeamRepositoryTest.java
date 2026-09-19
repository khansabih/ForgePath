package io.github.khansabih.forgepath.catalog.team.persistence;

import io.github.khansabih.forgepath.catalog.team.domain.Team;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
public class TeamRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:18-alpine");

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldSaveAndFindTeamById() {

        Team team = new Team("Messaging Team");

        teamRepository.saveAndFlush(team);

        entityManager.clear();

        Optional<Team> foundTeam =
                teamRepository.findById(team.getId());

        assertThat(foundTeam).isPresent();
        assertThat(foundTeam.get().getId())
                .isEqualTo(team.getId());
        assertThat(foundTeam.get().getName())
                .isEqualTo("Messaging Team");
        assertThat(foundTeam.get().getCreatedAt())
                .isNotNull();
    }

    @Test
    void shouldDetectExistingTeamByName() {

        Team team = new Team("Messaging Team");

        teamRepository.saveAndFlush(team);

        boolean exists =
                teamRepository.existsByName("Messaging Team");

        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnTeamsOrderedByName() {

        teamRepository.save(new Team("Payments Team"));
        teamRepository.save(new Team("Infrastructure Team"));
        teamRepository.save(new Team("Messaging Team"));

        teamRepository.flush();

        entityManager.clear();

        List<Team> teams =
                teamRepository.findAllByOrderByNameAsc();

        assertThat(teams)
                .extracting(Team::getName)
                .containsExactly(
                        "Infrastructure Team",
                        "Messaging Team",
                        "Payments Team"
                );
    }

    @Test
    void shouldRejectDuplicateTeamName() {

        teamRepository.saveAndFlush(
                new Team("Messaging Team")
        );

        assertThatThrownBy(() ->
                teamRepository.saveAndFlush(
                        new Team("Messaging Team")
                )
        ).isInstanceOf(DataIntegrityViolationException.class);
    }
}
