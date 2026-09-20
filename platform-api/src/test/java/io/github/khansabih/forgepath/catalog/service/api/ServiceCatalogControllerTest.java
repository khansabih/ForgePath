package io.github.khansabih.forgepath.catalog.service.api;

import io.github.khansabih.forgepath.catalog.service.application.ServiceCatalogService;
import io.github.khansabih.forgepath.catalog.service.domain.ServiceFramework;
import io.github.khansabih.forgepath.catalog.service.domain.ServiceRuntime;
import io.github.khansabih.forgepath.catalog.service.exception.PlatformServiceAlreadyExistsException;
import io.github.khansabih.forgepath.catalog.service.exception.PlatformServiceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ServiceCatalogController.class)
class ServiceCatalogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ServiceCatalogService serviceCatalogService;

    @Test
    void shouldCreateService() throws Exception {

        UUID serviceId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();

        PlatformServiceResponse response =
                new PlatformServiceResponse(
                        serviceId,
                        "notification-service",
                        "Sends notifications",
                        teamId,
                        "Messaging Team",
                        ServiceFramework.SPRING_BOOT,
                        ServiceRuntime.JAVA_21,
                        Instant.now(),
                        Instant.now()
                );

        when(
                serviceCatalogService.createPlatformService(
                        any(CreateServiceRequest.class)
                )
        ).thenReturn(response);

        mockMvc.perform(
                        post("/api/v1/services")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "name": "notification-service",
                                          "description": "Sends notifications",
                                          "ownerTeamId": "%s",
                                          "framework": "SPRING_BOOT",
                                          "runtime": "JAVA_21"
                                        }
                                        """.formatted(teamId))
                )
                .andExpect(status().isCreated())
                .andExpect(
                        header().string(
                                "Location",
                                "/api/v1/services/" + serviceId
                        )
                )
                .andExpect(
                        jsonPath("$.id")
                                .value(serviceId.toString())
                )
                .andExpect(
                        jsonPath("$.name")
                                .value("notification-service")
                );

        verify(serviceCatalogService)
                .createPlatformService(any(CreateServiceRequest.class));
    }

    @Test
    void shouldRejectBlankServiceName() throws Exception {

        UUID teamId = UUID.randomUUID();

        mockMvc.perform(
                        post("/api/v1/services")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "name": "",
                                          "description": "Sends notifications",
                                          "ownerTeamId": "%s",
                                          "framework": "SPRING_BOOT",
                                          "runtime": "JAVA_21"
                                        }
                                        """.formatted(teamId))
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(serviceCatalogService);
    }

    @Test
    void shouldReturnConflictForDuplicateService() throws Exception {

        when(
                serviceCatalogService.createPlatformService(
                        any(CreateServiceRequest.class)
                )
        ).thenThrow(
                new PlatformServiceAlreadyExistsException(
                        "notification-service"
                )
        );

        mockMvc.perform(
                        post("/api/v1/services")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "name": "notification-service",
                                          "ownerTeamId": "%s",
                                          "framework": "SPRING_BOOT",
                                          "runtime": "JAVA_21"
                                        }
                                        """.formatted(UUID.randomUUID()))
                )
                .andExpect(status().isConflict())
                .andExpect(
                        jsonPath("$.status")
                                .value(409)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Service already exists with name: notification-service"
                                )
                );
    }

    @Test
    void shouldReturnAllServices() throws Exception {

        UUID teamId = UUID.randomUUID();

        PlatformServiceResponse email =
                createResponse(
                        "email-service",
                        teamId
                );

        PlatformServiceResponse notification =
                createResponse(
                        "notification-service",
                        teamId
                );

        when(serviceCatalogService.getServices())
                .thenReturn(
                        List.of(email, notification)
                );

        mockMvc.perform(
                        get("/api/v1/services")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.length()")
                                .value(2)
                )
                .andExpect(
                        jsonPath("$[0].name")
                                .value("email-service")
                )
                .andExpect(
                        jsonPath("$[1].name")
                                .value("notification-service")
                );
    }

    @Test
    void shouldReturnServiceById() throws Exception {

        UUID serviceId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();

        PlatformServiceResponse response =
                new PlatformServiceResponse(
                        serviceId,
                        "notification-service",
                        "Sends notifications",
                        teamId,
                        "Messaging Team",
                        ServiceFramework.SPRING_BOOT,
                        ServiceRuntime.JAVA_21,
                        Instant.now(),
                        Instant.now()
                );

        when(
                serviceCatalogService.getService(serviceId)
        ).thenReturn(response);

        mockMvc.perform(
                        get(
                                "/api/v1/services/{serviceId}",
                                serviceId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(serviceId.toString())
                )
                .andExpect(
                        jsonPath("$.name")
                                .value("notification-service")
                );
    }

    @Test
    void shouldReturnNotFoundWhenServiceDoesNotExist()
            throws Exception {

        UUID serviceId = UUID.randomUUID();

        when(
                serviceCatalogService.getService(serviceId)
        ).thenThrow(
                new PlatformServiceNotFoundException(serviceId)
        );

        mockMvc.perform(
                        get(
                                "/api/v1/services/{serviceId}",
                                serviceId
                        )
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.status")
                                .value(404)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Service with id "
                                                + serviceId
                                                + " not found"
                                )
                );
    }

    @Test
    void shouldFilterServicesByOwnerTeam() throws Exception {

        UUID teamId = UUID.randomUUID();

        PlatformServiceResponse email =
                createResponse(
                        "email-service",
                        teamId
                );

        PlatformServiceResponse sms =
                createResponse(
                        "sms-service",
                        teamId
                );

        when(
                serviceCatalogService
                        .getServicesByOwnerTeam(teamId)
        ).thenReturn(
                List.of(email, sms)
        );

        mockMvc.perform(
                        get("/api/v1/services")
                                .param(
                                        "ownerTeamId",
                                        teamId.toString()
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.length()")
                                .value(2)
                )
                .andExpect(
                        jsonPath("$[0].name")
                                .value("email-service")
                )
                .andExpect(
                        jsonPath("$[1].name")
                                .value("sms-service")
                );

        verify(serviceCatalogService)
                .getServicesByOwnerTeam(teamId);

        verify(
                serviceCatalogService,
                never()
        ).getServices();
    }

    private PlatformServiceResponse createResponse(
            String name,
            UUID teamId
    ) {

        return new PlatformServiceResponse(
                UUID.randomUUID(),
                name,
                null,
                teamId,
                "Messaging Team",
                ServiceFramework.SPRING_BOOT,
                ServiceRuntime.JAVA_21,
                Instant.now(),
                Instant.now()
        );
    }
}