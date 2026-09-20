package io.github.khansabih.forgepath.catalog.service.application;

import io.github.khansabih.forgepath.catalog.service.api.CreateServiceRequest;
import io.github.khansabih.forgepath.catalog.service.api.PlatformServiceResponse;
import io.github.khansabih.forgepath.catalog.service.domain.PlatformService;
import io.github.khansabih.forgepath.catalog.service.domain.ServiceFramework;
import io.github.khansabih.forgepath.catalog.service.domain.ServiceRuntime;
import io.github.khansabih.forgepath.catalog.service.exception.PlatformServiceAlreadyExistsException;
import io.github.khansabih.forgepath.catalog.service.exception.PlatformServiceNotFoundException;
import io.github.khansabih.forgepath.catalog.service.persistence.PlatformServiceRepository;
import io.github.khansabih.forgepath.catalog.team.domain.Team;
import io.github.khansabih.forgepath.catalog.team.exception.TeamNotFoundException;
import io.github.khansabih.forgepath.catalog.team.persistence.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ServiceCatalogServiceTest {

    private PlatformServiceRepository platformServiceRepository;
    private TeamRepository teamRepository;
    private ServiceCatalogService serviceCatalogService;

    @BeforeEach
    void setUp() {

        platformServiceRepository =
                mock(PlatformServiceRepository.class);

        teamRepository =
                mock(TeamRepository.class);

        serviceCatalogService =
                new ServiceCatalogService(
                        platformServiceRepository,
                        teamRepository
                );
    }

    @Test
    void shouldCreateServiceForExistingTeam() {

        Team team = new Team("Messaging Team");

        CreateServiceRequest request =
                new CreateServiceRequest(
                        "notification-service",
                        "Sends notifications",
                        team.getId(),
                        ServiceFramework.SPRING_BOOT,
                        ServiceRuntime.JAVA_21
                );

        when(
                platformServiceRepository
                        .existsByName("notification-service")
        ).thenReturn(false);

        when(teamRepository.findById(team.getId()))
                .thenReturn(Optional.of(team));

        when(
                platformServiceRepository
                        .saveAndFlush(any(PlatformService.class))
        ).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        PlatformServiceResponse response =
                serviceCatalogService.createPlatformService(request);

        assertThat(response.name())
                .isEqualTo("notification-service");

        assertThat(response.ownerTeamId())
                .isEqualTo(team.getId());

        assertThat(response.ownerTeamName())
                .isEqualTo("Messaging Team");

        assertThat(response.framework())
                .isEqualTo(ServiceFramework.SPRING_BOOT);

        assertThat(response.runtime())
                .isEqualTo(ServiceRuntime.JAVA_21);

        verify(platformServiceRepository)
                .existsByName("notification-service");

        verify(teamRepository)
                .findById(team.getId());

        verify(platformServiceRepository)
                .saveAndFlush(any(PlatformService.class));
    }

    @Test
    void shouldRejectDuplicateServiceName() {

        CreateServiceRequest request =
                new CreateServiceRequest(
                        "notification-service",
                        "Sends notifications",
                        UUID.randomUUID(),
                        ServiceFramework.SPRING_BOOT,
                        ServiceRuntime.JAVA_21
                );

        when(
                platformServiceRepository
                        .existsByName("notification-service")
        ).thenReturn(true);

        assertThatThrownBy(
                () -> serviceCatalogService.createPlatformService(request)
        )
                .isInstanceOf(
                        PlatformServiceAlreadyExistsException.class
                )
                .hasMessageContaining("notification-service");

        verifyNoInteractions(teamRepository);

        verify(
                platformServiceRepository,
                never()
        ).saveAndFlush(any(PlatformService.class));
    }

    @Test
    void shouldRejectServiceWhenOwnerTeamDoesNotExist() {

        UUID teamId = UUID.randomUUID();

        CreateServiceRequest request =
                new CreateServiceRequest(
                        "notification-service",
                        "Sends notifications",
                        teamId,
                        ServiceFramework.SPRING_BOOT,
                        ServiceRuntime.JAVA_21
                );

        when(
                platformServiceRepository
                        .existsByName("notification-service")
        ).thenReturn(false);

        when(teamRepository.findById(teamId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> serviceCatalogService.createPlatformService(request)
        )
                .isInstanceOf(TeamNotFoundException.class);

        verify(
                platformServiceRepository,
                never()
        ).saveAndFlush(any(PlatformService.class));
    }

    @Test
    void shouldTranslateDatabaseDuplicateIntoDomainException() {

        Team team = new Team("Messaging Team");

        CreateServiceRequest request =
                new CreateServiceRequest(
                        "notification-service",
                        "Sends notifications",
                        team.getId(),
                        ServiceFramework.SPRING_BOOT,
                        ServiceRuntime.JAVA_21
                );

        when(
                platformServiceRepository
                        .existsByName("notification-service")
        ).thenReturn(false);

        when(teamRepository.findById(team.getId()))
                .thenReturn(Optional.of(team));

        when(
                platformServiceRepository
                        .saveAndFlush(any(PlatformService.class))
        ).thenThrow(
                new DataIntegrityViolationException(
                        "duplicate key"
                )
        );

        assertThatThrownBy(
                () -> serviceCatalogService.createPlatformService(request)
        )
                .isInstanceOf(
                        PlatformServiceAlreadyExistsException.class
                );
    }

    @Test
    void shouldReturnAllServicesOrderedByName() {

        Team team = new Team("Messaging Team");

        PlatformService emailService =
                new PlatformService(
                        "email-service",
                        "Sends emails",
                        team,
                        ServiceFramework.SPRING_BOOT,
                        ServiceRuntime.JAVA_21
                );

        PlatformService notificationService =
                new PlatformService(
                        "notification-service",
                        "Sends notifications",
                        team,
                        ServiceFramework.SPRING_BOOT,
                        ServiceRuntime.JAVA_21
                );

        when(
                platformServiceRepository
                        .findAllByOrderByNameAsc()
        ).thenReturn(
                List.of(
                        emailService,
                        notificationService
                )
        );

        List<PlatformServiceResponse> services =
                serviceCatalogService.getServices();

        assertThat(services)
                .hasSize(2);

        assertThat(services)
                .extracting(PlatformServiceResponse::name)
                .containsExactly(
                        "email-service",
                        "notification-service"
                );
    }

    @Test
    void shouldReturnServiceById() {

        Team team = new Team("Messaging Team");

        PlatformService service =
                new PlatformService(
                        "notification-service",
                        "Sends notifications",
                        team,
                        ServiceFramework.SPRING_BOOT,
                        ServiceRuntime.JAVA_21
                );

        UUID serviceId = service.getId();

        when(platformServiceRepository.findById(serviceId))
                .thenReturn(Optional.of(service));

        PlatformServiceResponse response =
                serviceCatalogService.getService(serviceId);

        assertThat(response.id())
                .isEqualTo(serviceId);

        assertThat(response.name())
                .isEqualTo("notification-service");

        assertThat(response.ownerTeamId())
                .isEqualTo(team.getId());
    }

    @Test
    void shouldThrowWhenServiceDoesNotExist() {

        UUID serviceId = UUID.randomUUID();

        when(platformServiceRepository.findById(serviceId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> serviceCatalogService.getService(serviceId)
        )
                .isInstanceOf(
                        PlatformServiceNotFoundException.class
                )
                .hasMessageContaining(serviceId.toString());
    }

    @Test
    void shouldReturnServicesOwnedByTeam() {

        Team messagingTeam =
                new Team("Messaging Team");

        PlatformService emailService =
                new PlatformService(
                        "email-service",
                        "Sends emails",
                        messagingTeam,
                        ServiceFramework.SPRING_BOOT,
                        ServiceRuntime.JAVA_21
                );

        PlatformService smsService =
                new PlatformService(
                        "sms-service",
                        "Sends SMS messages",
                        messagingTeam,
                        ServiceFramework.SPRING_BOOT,
                        ServiceRuntime.JAVA_21
                );

        when(
                teamRepository.existsById(
                        messagingTeam.getId()
                )
        ).thenReturn(true);

        when(
                platformServiceRepository
                        .findAllByOwnerTeam_IdOrderByNameAsc(
                                messagingTeam.getId()
                        )
        ).thenReturn(
                List.of(emailService, smsService)
        );

        List<PlatformServiceResponse> services =
                serviceCatalogService
                        .getServicesByOwnerTeam(
                                messagingTeam.getId()
                        );

        assertThat(services)
                .extracting(PlatformServiceResponse::name)
                .containsExactly(
                        "email-service",
                        "sms-service"
                );
    }

    @Test
    void shouldRejectServiceLookupWhenOwnerTeamDoesNotExist() {

        UUID teamId = UUID.randomUUID();

        when(teamRepository.existsById(teamId))
                .thenReturn(false);

        assertThatThrownBy(
                () -> serviceCatalogService
                        .getServicesByOwnerTeam(teamId)
        )
                .isInstanceOf(TeamNotFoundException.class);

        verify(
                platformServiceRepository,
                never()
        ).findAllByOwnerTeam_IdOrderByNameAsc(any(UUID.class));
    }
}