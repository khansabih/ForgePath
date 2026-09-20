package io.github.khansabih.forgepath.catalog.service.persistence;

import io.github.khansabih.forgepath.catalog.service.domain.PlatformService;
import io.github.khansabih.forgepath.catalog.service.domain.ServiceFramework;
import io.github.khansabih.forgepath.catalog.service.domain.ServiceRuntime;
import io.github.khansabih.forgepath.catalog.team.domain.Team;
import io.github.khansabih.forgepath.catalog.team.persistence.TeamRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class PlatformServiceRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:18-alpine");

    @Autowired
    private PlatformServiceRepository platformServiceRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldSaveAndFindServiceById(){
        Team team = teamRepository.saveAndFlush(new Team("Messaging Team"));
        PlatformService service = new PlatformService(
                "notification-service",
                "Sends application notifications",
                team,
                ServiceFramework.SPRING_BOOT,
                ServiceRuntime.JAVA_21
        );

        platformServiceRepository.saveAndFlush(service);

        entityManager.clear();

        Optional<PlatformService> foundService = platformServiceRepository.findById(service.getId());
        assertThat(foundService).isPresent();
        assertThat(foundService.get().getName()).isEqualTo("notification-service");
        assertThat(foundService.get().getDescription()).isEqualTo("Sends application notifications");
        assertThat(foundService.get().getOwnerTeam().getId()).isEqualTo(team.getId());
        assertThat(foundService.get().getFramework()).isEqualTo(ServiceFramework.SPRING_BOOT);
        assertThat(foundService.get().getRuntime()).isEqualTo(ServiceRuntime.JAVA_21);
    }

    @Test
    void shouldDetectExistingServiceByName(){
        Team team = teamRepository.saveAndFlush(new Team("Messaging Team"));

        platformServiceRepository.saveAndFlush(
                new PlatformService(
                        "notification-service",
                        "Sends notifications",
                        team,
                        ServiceFramework.SPRING_BOOT,
                        ServiceRuntime.JAVA_21
                )
        );

        entityManager.clear();

        boolean exists = platformServiceRepository.existsByName("notification-service");
        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnServicesOwnedByTeamOrderedByName(){
        Team messagingTeam = teamRepository.saveAndFlush(new Team("Messaging Team"));
        Team paymentsTeam = teamRepository.saveAndFlush(new Team("Playmens Team"));

        platformServiceRepository.save(
                new PlatformService(
                        "sms-service",
                        "Sends SMS notifications",
                        messagingTeam,
                        ServiceFramework.SPRING_BOOT,
                        ServiceRuntime.JAVA_21
                )
        );

        platformServiceRepository.save(
                new PlatformService(
                        "email-service",
                        "Sends emails",
                        messagingTeam,
                        ServiceFramework.SPRING_BOOT,
                        ServiceRuntime.JAVA_21
                )
        );

        platformServiceRepository.save(
                new PlatformService(
                        "payment-service",
                        "Processes payments",
                        paymentsTeam,
                        ServiceFramework.SPRING_BOOT,
                        ServiceRuntime.JAVA_21
                )
        );

        platformServiceRepository.flush();
        entityManager.clear();

        List<PlatformService> services = platformServiceRepository.findAllByOwnerTeam_IdOrderByNameAsc(messagingTeam.getId());

        assertThat(services).extracting(PlatformService::getName)
                .containsExactly("email-service", "sms-service");
    }

    @Test
    void shouldRejectDuplicateServiceName() {
        Team team = teamRepository.saveAndFlush(
                new Team("Messaging Team")
        );

        platformServiceRepository.saveAndFlush(
                new PlatformService(
                        "notification-service",
                        "First service",
                        team,
                        ServiceFramework.SPRING_BOOT,
                        ServiceRuntime.JAVA_21
                )
        );

        assertThatThrownBy(() ->
                platformServiceRepository.saveAndFlush(
                        new PlatformService(
                                "notification-service",
                                "Duplicate service",
                                team,
                                ServiceFramework.SPRING_BOOT,
                                ServiceRuntime.JAVA_21
                        )
                )
        ).isInstanceOf(DataIntegrityViolationException.class);
    }
}
