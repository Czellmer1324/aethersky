package com.czellmer1324.mastercontrol.entity;

import lombok.*;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IslandWorldCache {
    private UUID id;
    private byte[] worldData;
    private boolean isNew;
}
