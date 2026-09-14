CREATE TABLE teams(
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE ,
    create_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE services(
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    owner_team_id UUID NOT NULL,
    framework VARCHAR(50) NOT NULL,
    runtime VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_service_owner_team
                     FOREIGN KEY (owner_team_id)
                     REFERENCES teams(id)
);

CREATE INDEX idx_services_owner_team_id ON services(owner_team_id);