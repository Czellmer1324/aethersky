package com.czellmer1324.mastercontrol.service;

import com.czellmer1324.dto.PlayerData;
import com.czellmer1324.mastercontrol.entity.MasterPlayer;
import com.czellmer1324.mastercontrol.entity.MasterPlayerCache;
import com.czellmer1324.mastercontrol.master.dto.ServiceResponse;
import com.czellmer1324.mastercontrol.repository.MasterPlayerRedis;
import com.czellmer1324.mastercontrol.repository.MasterPlayerRepository;
import com.czellmer1324.mastercontrol.util.MasterPlayerMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MasterPlayerService {
    private final MasterPlayerRepository playerRepository;
    private final MasterPlayerMapper mapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final MasterPlayerRedis playerCache;
    private static final String WRITE_BUFFER_KEY = "players:pending_sync";

    //TODO: Implement redis cache for player info
    public ServiceResponse getPlayer(UUID uuid) {
        // check cache first
        Optional<MasterPlayerCache> cachedInfo = playerCache.findById(uuid);
        MasterPlayer player;

        // Cache miss they are not cached
        if (cachedInfo.isEmpty()) {
            Optional<MasterPlayer> opPlayer = playerRepository.findById(uuid);

            // Create a new player object if they have never been on the server
            player = opPlayer.orElseGet(() -> new MasterPlayer(uuid));

            //Cache them
            playerCache.save(mapper.toMasterPlayerCache(player));
        } else {
            player = mapper.toMasterPlayer(cachedInfo.get());
            log.info("Retrieved player from cache");
        }

        PlayerData data = new PlayerData(player.getUuid());
        log.info(data.uuid().toString());
        return new ServiceResponse(data, true, "no fail");
    }

    public ServiceResponse storePlayer(PlayerData data) {
        // Will save the info to cache here rather than update the database, for now update the database
        Optional<MasterPlayerCache> opPlayer = playerCache.findById(data.uuid());
        try {
            if (opPlayer.isEmpty()) {
                Optional<MasterPlayer> opRepoPlayer = playerRepository.findById(data.uuid());
                if (opRepoPlayer.isEmpty()) {
                    // Cache the new player info
                    playerCache.save(new MasterPlayerCache(data.uuid()));
                } else {
                    MasterPlayer player = opRepoPlayer.get();

                    //Update the player info
                    player.updateInfo(data);

                    // cache the player
                    playerCache.save(mapper.toMasterPlayerCache(player));
                }
            } else {
                // Update the information in player cache
                MasterPlayerCache player = opPlayer.get();
                player.updateInfo(data);
                playerCache.save(player);
            }

            // Mark for database sync
            redisTemplate.opsForSet().add(WRITE_BUFFER_KEY, data.uuid());
            log.info("Saved player with uuid: {}", data.uuid());

            return new ServiceResponse(Map.of("Message", "Player stored"), true, "No fail");
        } catch (Exception e) {
            return new ServiceResponse(Map.of("Message", "Something went wrong"), false, "Failure saving to database");
        }
    }
}
