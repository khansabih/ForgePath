package io.github.khansabih.forgepath.catalog.service.api;

import io.github.khansabih.forgepath.catalog.service.application.ServiceCatalogService;
import io.github.khansabih.forgepath.catalog.service.persistence.PlatformServiceRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/services")
@RequiredArgsConstructor
public class ServiceCatalogController {

    private final ServiceCatalogService serviceCatalogService;

    @PostMapping
    public ResponseEntity<PlatformServiceResponse> createService(
            @Valid @RequestBody CreateServiceRequest request
    ){
        PlatformServiceResponse serviceResponse = serviceCatalogService.createPlatformService(request);
        URI location = URI.create("/api/v1/services/" + serviceResponse.id());
        return ResponseEntity.created(location).body(serviceResponse);
    }

    @GetMapping
    public ResponseEntity<List<PlatformServiceResponse>> getServices(
            @RequestParam(required = false) UUID ownerTeamId
    ){
        if(ownerTeamId != null){
            return ResponseEntity.ok(
                    serviceCatalogService.getServicesByOwnerTeam(ownerTeamId)
            );
        }

        return ResponseEntity.ok(serviceCatalogService.getServices());
    }

    @GetMapping("/{serviceId}")
    public ResponseEntity<PlatformServiceResponse> getService(
            @PathVariable UUID serviceId
    ){
        return ResponseEntity.ok(serviceCatalogService.getService(serviceId));
    }
}
