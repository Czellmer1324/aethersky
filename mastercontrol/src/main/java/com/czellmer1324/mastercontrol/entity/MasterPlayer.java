package com.czellmer1324.mastercontrol.entity;

import com.czellmer1324.dto.PlayerData;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Persistable;

import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "master_player")
@NoArgsConstructor
public class MasterPlayer implements Persistable<UUID> {
    @Id
    private UUID uuid;

    public MasterPlayer(UUID uuid) {
        this.uuid = uuid;
    }

    public void updateInfo(PlayerData data) {
        this.uuid = data.uuid();
    }


    @Transient
    private boolean isNew = true; // Flag to track if it's new

    @Override
    public UUID getId() { return uuid; }

    @Override
    public boolean isNew() { return isNew; }

    @PostLoad
    @PrePersist
    void markNotNew() {
        this.isNew = false;
    }

}
