package io.github.khansabih.forgepath.catalog.service.exception;

import io.github.khansabih.forgepath.catalog.service.domain.ServiceRuntime;

public class PlatformServiceAlreadyExistsException extends RuntimeException {
    public PlatformServiceAlreadyExistsException(String name) {
        super("Service already exists with name: " + name);
    }
}
