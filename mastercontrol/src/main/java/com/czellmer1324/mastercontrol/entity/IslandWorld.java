package com.czellmer1324.mastercontrol.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

// The class for storing the raw Island world byte[]

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class IslandWorld {
    // Have it as string so default world can be named something
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "world_data", nullable = false, columnDefinition = "BYTEA")
    private byte[] worldData;

    public IslandWorld(UUID ownerId, byte[] worldData) {
        this.ownerId = ownerId;
        this.worldData = worldData;
    }
}
