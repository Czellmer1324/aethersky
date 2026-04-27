package com.czellmer1324.mastercontrol.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "master_player")
public class MasterPlayer {
    @Id
    private UUID uuid;

    public MasterPlayer(UUID uuid) {
        this.uuid = uuid;
    }
}
