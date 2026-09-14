package io.github.khansabih.forgepath.catalog.team.exception;

import java.util.UUID;

public class TeamNotFoundException extends RuntimeException{
    public TeamNotFoundException(UUID teamId){
        super("Team with id "+teamId+" not found");
    }
}
