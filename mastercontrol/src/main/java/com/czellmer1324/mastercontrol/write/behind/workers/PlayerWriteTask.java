package com.czellmer1324.mastercontrol.write.behind.workers;

import com.czellmer1324.mastercontrol.entity.MasterPlayer;
import com.czellmer1324.mastercontrol.entity.MasterPlayerCache;
import com.czellmer1324.mastercontrol.repository.MasterPlayerRepository;
import com.czellmer1324.mastercontrol.util.MasterPlayerMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
@AllArgsConstructor
@Slf4j
public class PlayerWriteTask {
    private final RedisTemplate<String, Object> redisTemplate;
    private final MasterPlayerRepository playerRepository;
    private final MasterPlayerMapper mapper;

    @Scheduled(initialDelay = 1, fixedRate = 1, timeUnit = TimeUnit.MINUTES)
    public void batchSavePlayers() {
        Set<Object> pendingSync = redisTemplate.opsForSet().members("players:pending_sync");
        if (pendingSync == null || pendingSync.isEmpty()) {
            return;
        }

        // Collect all players needing to be saved
        List<MasterPlayer> playersToSync = pendingSync.stream()
                .map(id -> {
                    try {
                        UUID uuid = UUID.fromString(id.toString());
                        MasterPlayerCache cachePlayer = (MasterPlayerCache) redisTemplate.opsForHash().get("PLAYERS", Collections.singleton("player:" + uuid));

                        if (cachePlayer == null) {
                            throw new EntityNotFoundException("Player not found in cache");
                        }

                        return mapper.toMasterPlayer(cachePlayer);
                    } catch (Exception e) {
                        log.warn("Failed to process player {}:{}", id, e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();

        try {
            //save the players
            playerRepository.saveAll(playersToSync);
            redisTemplate.delete("players:pending_sync");
        } catch (Exception e) {
            log.warn("Failed to batch update players : {}", e.getMessage());
        }
    }

}
