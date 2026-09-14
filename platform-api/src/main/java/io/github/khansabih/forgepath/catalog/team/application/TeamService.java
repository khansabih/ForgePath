package io.github.khansabih.forgepath.catalog.team.application;

import io.github.khansabih.forgepath.catalog.team.api.CreateTeamRequest;
import io.github.khansabih.forgepath.catalog.team.api.TeamResponse;
import io.github.khansabih.forgepath.catalog.team.domain.Team;
import io.github.khansabih.forgepath.catalog.team.exception.TeamAlreadyExistsException;
import io.github.khansabih.forgepath.catalog.team.exception.TeamNotFoundException;
import io.github.khansabih.forgepath.catalog.team.persistence.TeamRepository;
import jakarta.transaction.TransactionScoped;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class TeamService {
    private final TeamRepository teamRepository;

    public TeamService(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    public TeamResponse createTeam(CreateTeamRequest createTeamRequest) {
        String normalizeTeamName = createTeamRequest.name().trim();
        if(teamRepository.existsByName(normalizeTeamName)) {
            throw new TeamAlreadyExistsException(normalizeTeamName);
        }

        Team team = new Team(normalizeTeamName);
        try {
            /*
            * With save(team);
                - Hibernate may delay the actual SQL INSERT until later in the transaction.

              With saveAndFlush(team);
                - we force Hibernate to send the insert to PostgreSQL here.
            *
            */
            Team savedTeam = teamRepository.saveAndFlush(team);
            return TeamResponse.from(savedTeam);
        }catch(DataIntegrityViolationException e) {
            throw new TeamAlreadyExistsException(normalizeTeamName);
        }
    }

    @Transactional(readOnly = true)
    public List<TeamResponse> getTeams() {
        return teamRepository.findAllByOrderByNameAsc()
                .stream()
                .map(TeamResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public TeamResponse getTeam(UUID teamId) {
        return teamRepository.findById(teamId)
                .map(TeamResponse::from)
                .orElseThrow(() -> new TeamNotFoundException(teamId));
    }
}
