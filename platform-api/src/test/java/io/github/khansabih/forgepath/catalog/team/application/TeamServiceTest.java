package io.github.khansabih.forgepath.catalog.team.application;

import io.github.khansabih.forgepath.catalog.team.api.CreateTeamRequest;
import io.github.khansabih.forgepath.catalog.team.api.TeamResponse;
import io.github.khansabih.forgepath.catalog.team.domain.Team;
import io.github.khansabih.forgepath.catalog.team.exception.TeamAlreadyExistsException;
import io.github.khansabih.forgepath.catalog.team.exception.TeamNotFoundException;
import io.github.khansabih.forgepath.catalog.team.persistence.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class TeamServiceTest {
    private TeamRepository teamRepository;
    private TeamService teamService;

    @BeforeEach
    public void setup() {
        teamRepository = mock(TeamRepository.class);
        teamService = new TeamService(teamRepository);
    }

    @Test
    void shouldCreateTeam() {
        CreateTeamRequest request = new CreateTeamRequest("Messaging Team");
        when(teamRepository.existsByName("Messaging Team")).thenReturn(false);
        when(teamRepository.saveAndFlush(any(Team.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TeamResponse teamResponse = teamService.createTeam(request);

        assertThat(teamResponse.name())
                .isEqualTo("Messaging Team");

        assertThat(teamResponse.id()).isNotNull();

        assertThat(teamResponse.created_at()).isNotNull();

        verify(teamRepository).existsByName("Messaging Team");

        verify(teamRepository).saveAndFlush(any(Team.class));
    }

    @Test
    void shouldRejectDuplicateTeam() {
        CreateTeamRequest request = new CreateTeamRequest("Messaging Team");
        when(teamRepository.existsByName("Messaging Team")).thenReturn(true);
        assertThatThrownBy(() -> teamService.createTeam(request))
                .isInstanceOf(TeamAlreadyExistsException.class)
                .hasMessageContaining("Messaging Team");

        verify(teamRepository).existsByName("Messaging Team");
        verify(teamRepository, never()).saveAndFlush(any(Team.class));
    }

    @Test
    void shouldReturnTeamsOrderedByName(){
        Team messagingTeam = new Team("Messaging Team");
        Team catalogTeam = new Team("Catalog Team");
        Team paymentTeam = new Team("Payment Team");

        when(teamRepository.findAllByOrderByNameAsc())
                .thenReturn(Arrays.asList(catalogTeam, messagingTeam, paymentTeam));

        List<TeamResponse> teams = teamService.getTeams();
        assertThat(teams).hasSize(3);
        assertThat(teams.get(0).name()).isEqualTo("Catalog Team");
        assertThat(teams.get(1).name()).isEqualTo("Messaging Team");
        assertThat(teams.get(2).name()).isEqualTo("Payment Team");
    }

    @Test
    void shouldReturnTeamById(){
        Team team = new Team("Messaging Team");
        UUID teamId = team.getId();
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));

        TeamResponse teamResponse = teamService.getTeam(teamId);

        assertThat(teamResponse.id()).isEqualTo(teamId);
        assertThat(teamResponse.name()).isEqualTo("Messaging Team");
    }

    @Test
    void shouldThrowWhenTeamDoesNotExist() {

        UUID teamId = UUID.randomUUID();

        when(teamRepository.findById(teamId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.getTeam(teamId))
                .isInstanceOf(TeamNotFoundException.class)
                .hasMessageContaining(teamId.toString());
    }
}
