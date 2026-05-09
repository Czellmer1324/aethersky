package com.czellmer1324.mastercontrol.entity;

import com.czellmer1324.dto.PlayerData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MasterPlayerCache {
    private UUID uuid;

    private boolean isNew;

    public void updateInfo(PlayerData data) {
        this.uuid = data.uuid();
    }
}
