package io.github.khansabih.forgepath.catalog.team.api;

import io.github.khansabih.forgepath.catalog.team.application.TeamService;
import io.github.khansabih.forgepath.catalog.team.exception.TeamAlreadyExistsException;
import io.github.khansabih.forgepath.catalog.team.exception.TeamNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TeamController.class)
public class TeamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TeamService teamService;

    @Test
    void shouldCreateTeam() throws Exception {
        UUID teamId =
                UUID.fromString("7ea550be-4a6a-4055-81c2-eb804adb414c");

        TeamResponse response = new TeamResponse(
                teamId,
                "Messaging Team",
                Instant.parse("2026-09-13T19:59:43Z")
        );

        when(teamService.createTeam(any(CreateTeamRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                post("/api/v1/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Messaging Team"
                                }
                                """)
        ).andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/teams/" + teamId))
                .andExpect(jsonPath("$.id").value(teamId.toString()))
                .andExpect(jsonPath("$.name").value("Messaging Team"));

        verify(teamService)
                .createTeam(any(CreateTeamRequest.class));
    }

    @Test
    void shouldRejectBlankTeamName() throws Exception {

        mockMvc.perform(
                        post("/api/v1/teams")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "name": ""
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status")
                        .value(400))
                .andExpect(jsonPath("$.error")
                        .value("Bad Request"));

        verifyNoInteractions(teamService);
    }

    @Test
    void shouldReturnConflictForDuplicateTeam() throws Exception {

        when(teamService.createTeam(any(CreateTeamRequest.class)))
                .thenThrow(
                        new TeamAlreadyExistsException("Messaging Team")
                );

        mockMvc.perform(
                        post("/api/v1/teams")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "name": "Messaging Team"
                                        }
                                        """)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status")
                        .value(409))
                .andExpect(jsonPath("$.error")
                        .value("Conflict"))
                .andExpect(jsonPath("$.message")
                        .value(
                                "Team already exists with name: Messaging Team"
                        ));
    }

    @Test
    void shouldReturnAllTeams() throws Exception {

        TeamResponse messagingTeam = new TeamResponse(
                UUID.randomUUID(),
                "Messaging Team",
                Instant.now()
        );

        TeamResponse paymentsTeam = new TeamResponse(
                UUID.randomUUID(),
                "Payments Team",
                Instant.now()
        );

        when(teamService.getTeams())
                .thenReturn(List.of(messagingTeam, paymentsTeam));

        mockMvc.perform(
                        get("/api/v1/teams")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()")
                        .value(2))
                .andExpect(jsonPath("$[0].name")
                        .value("Messaging Team"))
                .andExpect(jsonPath("$[1].name")
                        .value("Payments Team"));
    }

    @Test
    void shouldReturnTeamById() throws Exception {

        UUID teamId = UUID.randomUUID();

        TeamResponse response = new TeamResponse(
                teamId,
                "Messaging Team",
                Instant.now()
        );

        when(teamService.getTeam(teamId))
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/v1/teams/{teamId}", teamId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(teamId.toString()))
                .andExpect(jsonPath("$.name")
                        .value("Messaging Team"));
    }

    @Test
    void shouldReturnNotFoundWhenTeamDoesNotExist() throws Exception {

        UUID teamId = UUID.randomUUID();

        when(teamService.getTeam(teamId))
                .thenThrow(new TeamNotFoundException(teamId));

        mockMvc.perform(
                        get("/api/v1/teams/{teamId}", teamId)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status")
                        .value(404))
                .andExpect(jsonPath("$.error")
                        .value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Team with id " + teamId + " not found"));
    }
}
