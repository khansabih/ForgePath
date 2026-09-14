package io.github.khansabih.forgepath.catalog.team.api;

import io.github.khansabih.forgepath.catalog.team.application.TeamService;
import io.github.khansabih.forgepath.catalog.team.domain.Team;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/teams")
public class TeamController {
    private final TeamService teamService;
    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @PostMapping
    public ResponseEntity<TeamResponse> createTeam(
            @Valid  @RequestBody CreateTeamRequest teamRequest) {
        TeamResponse teamResponse = teamService.createTeam(teamRequest);
        URI location = URI.create("/api/v1/teams/" + teamResponse.id());

        return ResponseEntity
                .created(location)
                .body(teamResponse);
    }

    @GetMapping
    public ResponseEntity<List<TeamResponse>> getTeams() {
        return ResponseEntity.ok(teamService.getTeams());
    }

    @GetMapping("/{teamId}")
    public ResponseEntity<TeamResponse> getTeam(
            @PathVariable UUID teamId
            ){
        return ResponseEntity.ok(teamService.getTeam(teamId));
    }
}
