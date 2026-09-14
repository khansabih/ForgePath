package io.github.khansabih.forgepath.catalog.team.exception;

public class TeamAlreadyExistsException extends RuntimeException{
    public TeamAlreadyExistsException(String name){
        super("Team already exists with name: " + name);
    }
}
