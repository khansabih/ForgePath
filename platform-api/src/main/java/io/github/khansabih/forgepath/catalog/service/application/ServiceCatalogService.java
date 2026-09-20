package io.github.khansabih.forgepath.catalog.service.application;

import io.github.khansabih.forgepath.catalog.service.api.CreateServiceRequest;
import io.github.khansabih.forgepath.catalog.service.api.PlatformServiceResponse;
import io.github.khansabih.forgepath.catalog.service.domain.PlatformService;
import io.github.khansabih.forgepath.catalog.service.exception.PlatformServiceAlreadyExistsException;
import io.github.khansabih.forgepath.catalog.service.exception.PlatformServiceNotFoundException;
import io.github.khansabih.forgepath.catalog.service.persistence.PlatformServiceRepository;
import io.github.khansabih.forgepath.catalog.team.domain.Team;
import io.github.khansabih.forgepath.catalog.team.exception.TeamNotFoundException;
import io.github.khansabih.forgepath.catalog.team.persistence.TeamRepository;
import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class ServiceCatalogService {

    private final PlatformServiceRepository platformServiceRepository;
    private final TeamRepository teamRepository;

    public PlatformServiceResponse createPlatformService(CreateServiceRequest request) {
        String normalizedName = request.name().trim();
        if(platformServiceRepository.existsByName(normalizedName)) {
            throw new PlatformServiceAlreadyExistsException(normalizedName);
        }

        Team ownerTeam = teamRepository.findById(request.ownerTeamId())
                .orElseThrow(() -> new TeamNotFoundException(request.ownerTeamId()));

        String normalizedDescription = request.description() == null ? null : request.description().trim();
        PlatformService platformService = new PlatformService(
                normalizedName,
                normalizedDescription,
                ownerTeam,
                request.framework(),
                request.runtime()
        );

        try{
            PlatformService savedService = platformServiceRepository.saveAndFlush(platformService);
            return PlatformServiceResponse.from(savedService);
        }catch(DataIntegrityViolationException e) {
            throw new PlatformServiceAlreadyExistsException(normalizedName);
        }
    }

    @Transactional(readOnly = true)
    public List<PlatformServiceResponse> getServices(){
        return platformServiceRepository.findAllByOrderByNameAsc()
                .stream().map(PlatformServiceResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PlatformServiceResponse getService(UUID serviceId){
        return platformServiceRepository.findById(serviceId)
                .map(PlatformServiceResponse::from)
                .orElseThrow(() -> new PlatformServiceNotFoundException(serviceId));
    }

    @Transactional(readOnly = true)
    public List<PlatformServiceResponse> getServicesByOwnerTeam(UUID ownerTeamId){
        if(!teamRepository.existsById(ownerTeamId)){
            throw new TeamNotFoundException(ownerTeamId);
        }

        return platformServiceRepository.findAllByOwnerTeam_IdOrderByNameAsc(ownerTeamId)
                .stream().map(PlatformServiceResponse::from).toList();
    }
}
