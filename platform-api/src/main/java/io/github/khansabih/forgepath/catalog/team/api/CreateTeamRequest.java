package io.github.khansabih.forgepath.catalog.team.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTeamRequest(
    @NotBlank(message = "Team name is required")
    @Size(max = 100, message = "Team name must not esceed 100 characters")
    String name
) {
}
