package com.czellmer1324.mastercontrol.service;

import com.czellmer1324.dto.PlayerData;
import com.czellmer1324.mastercontrol.entity.MasterPlayer;
import com.czellmer1324.mastercontrol.entity.MasterPlayerCache;
import com.czellmer1324.mastercontrol.master.dto.ServiceResponse;
import com.czellmer1324.mastercontrol.repository.MasterPlayerRepository;
import com.czellmer1324.mastercontrol.util.MasterPlayerMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class MasterPlayerService {
    private final MasterPlayerRepository playerRepository;
    private final MasterPlayerMapper mapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String WRITE_BUFFER_KEY = "players:pending_sync";
    private static final String PLAYER_HASH_KEY = "PLAYERS";
    private static final int TTL_MINUTES = 5;

    public ServiceResponse getPlayer(UUID uuid) {
        try {
            // check cache first
            Optional<MasterPlayerCache> cachedInfo = getFromCache(uuid);
            MasterPlayer player;

            // Cache miss they are not cached
            if (cachedInfo.isEmpty()) {
                Optional<MasterPlayer> opPlayer = playerRepository.findById(uuid);

                // Create a new player object if they have never been on the server
                player = opPlayer.orElseGet(() -> new MasterPlayer(uuid));

                if (player.isNew()) {
                    // Save the player if a new one is created into the database
                    player = playerRepository.save(player);
                }

                // cache them
                cachePlayer(mapper.toMasterPlayerCache(player));
                // Set expiration
                setNewTTL(uuid);
            } else {
                player = mapper.toMasterPlayer(cachedInfo.get());
                //update the TTL of the cached player
                setNewTTL(uuid);
            }

            PlayerData data = new PlayerData(player.getUuid(), player.getInventory());
            return new ServiceResponse(data, true, "no fail");
        } catch (Exception e) {
            log.warn(e.getMessage());
            return new ServiceResponse(Map.of("Message", "Failure retrieving player"), false, "Failure retrieving player info");
        }
    }

    public ServiceResponse storePlayer(PlayerData data) {
        Optional<MasterPlayerCache> opPlayer = getFromCache(data.uuid());
        try {
            if (opPlayer.isEmpty()) {
                // In case player is not cached
                Optional<MasterPlayer> opRepoPlayer = playerRepository.findById(data.uuid());

                MasterPlayer player;

                if (opRepoPlayer.isEmpty()) {
                    player = playerRepository.save(new MasterPlayer(data));
                } else {
                    player = opRepoPlayer.get();

                    //Update the player info
                    player.updateInfo(data);
                }

                cachePlayer(mapper.toMasterPlayerCache(player));
                setNewTTL(player.getUuid());
            } else {
                // Update the information in player cache
                MasterPlayerCache player = opPlayer.get();
                player.updateInfo(data);

                cachePlayer(player);
                setNewTTL(player.getUuid());
            }

            // Mark for database sync
            redisTemplate.opsForSet().add(WRITE_BUFFER_KEY, data.uuid());

            return new ServiceResponse(Map.of("Message", "Player stored"), true, "No fail");
        } catch (Exception e) {
            log.warn("Error trying to store player: {}", e.getMessage());
            return new ServiceResponse(Map.of("Message", "Something went wrong"), false, "Failure saving to database");
        }
    }

    private Optional<MasterPlayerCache> getFromCache(UUID uuid) {
        MasterPlayerCache cachedPlayer = (MasterPlayerCache) redisTemplate.opsForHash().get(PLAYER_HASH_KEY, "player:" + uuid.toString());

        if (cachedPlayer == null) {
            return Optional.empty();
        } else {
            return Optional.of(cachedPlayer);
        }
    }

    private void cachePlayer(MasterPlayerCache player) {
        redisTemplate.opsForHash().put(PLAYER_HASH_KEY, "player:" + player.getUuid().toString(), player);
    }

    private void setNewTTL(UUID uuid) {
        redisTemplate.opsForHash().expire(PLAYER_HASH_KEY, Duration.ofMinutes(TTL_MINUTES), Collections.singleton("player:" + uuid));
    }
}
