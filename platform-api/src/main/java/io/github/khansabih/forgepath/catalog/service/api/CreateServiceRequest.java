package io.github.khansabih.forgepath.catalog.service.api;

import io.github.khansabih.forgepath.catalog.service.domain.ServiceFramework;
import io.github.khansabih.forgepath.catalog.service.domain.ServiceRuntime;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateServiceRequest(
        @NotBlank(message = "Service name is required")
        @Size(max = 100, message = "Service name should not exceed 100 characters")
        String name,

        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description,

        @NotNull(message = "Owner team is required")
        UUID ownerTeamId,

        @NotNull(message = "Framework is required")
        ServiceFramework framework,

        @NotNull(message = "Runtime is required")
        ServiceRuntime runtime
) {
}
