package io.github.khansabih.forgepath.catalog.service.exception;

import java.util.UUID;

public class PlatformServiceNotFoundException extends RuntimeException{
    public PlatformServiceNotFoundException(UUID serviceId) {
        super("Service with id " +  serviceId + " not found");
    }
}
