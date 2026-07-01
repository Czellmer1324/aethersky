package com.czellmer1324.mastercontrol.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Persistable;

import java.util.UUID;

// The class for storing the raw Island world byte[]

@Entity
@Getter
@Setter
@NoArgsConstructor
public class IslandWorld implements Persistable<UUID> {
    // Have it as string so default world can be named something
    @Id
    private UUID id;

    @Column(name = "world_data", nullable = false, columnDefinition = "BYTEA")
    private byte[] worldData;

    public IslandWorld(UUID id, byte[] worldData) {
        this.id = id;
        this.worldData = worldData;
    }

    @Transient
    private boolean isNew = true; // Flag to track if it's new

    @Override
    public UUID getId() { return id; }

    @Override
    public boolean isNew() { return isNew; }

    @PostLoad
    @PrePersist
    void markNotNew() {
        this.isNew = false;
    }
}
