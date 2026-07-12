package com.czellmer1324.mastercontrol.service;

import com.czellmer1324.mastercontrol.entity.IslandWorld;
import com.czellmer1324.mastercontrol.entity.IslandWorldCache;
import com.czellmer1324.mastercontrol.master.dto.ServiceResponse;
import com.czellmer1324.mastercontrol.repository.IslandWorldRepository;
import com.czellmer1324.mastercontrol.util.IslandWorldMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class IslandService {
    private static final UUID DEFAULT_ISLAND_ID = UUID.fromString("5f71dda6-2f3a-41ea-a6c6-65365aaddbc0");
    // TODO: Add caching after basic read from database logic is there
    private final IslandWorldRepository worldRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String WRITE_BUFFER_KEY = "island_worlds:pending_sync";
    private static final int TTL_MINUTES = 5;

    public ServiceResponse getIsland(UUID ownerId) {
        Optional<IslandWorld> opWorld = worldRepository.findById(ownerId);
        IslandWorld world;

        if (opWorld.isEmpty()) {
            // Fall back to default world blob
            Optional<IslandWorld> opDefault = worldRepository.findById(DEFAULT_ISLAND_ID);
            if (opDefault.isEmpty()) {
                return new ServiceResponse(null, false, "Something went wrong with grabbing default world");
            }

            IslandWorld defaultWorld = opDefault.get();

            world = new IslandWorld(ownerId, defaultWorld.getWorldData());
            world = worldRepository.save(world);
        } else {
            world = opWorld.get();
        }

        return new ServiceResponse(world.getWorldData(), true, null);
    }

    private boolean islandExists(UUID ownerId) {
        return worldRepository.existsById(ownerId);
    }

    public ServiceResponse saveIsland(UUID ownerId, byte[] worldData) {
        log.info("Save island was triggered");
        try {
            IslandWorldCache islandData = new IslandWorldCache(ownerId, worldData, false);
            redisTemplate.opsForSet().add(WRITE_BUFFER_KEY, islandData);
        } catch (Exception e) {
            log.warn("Error saving island for user {}: {}", ownerId, e.getMessage());
            return new ServiceResponse(Map.of("Message", "Failed to save island"), false, "Failed to save");
        }

        return new ServiceResponse(Map.of("Message", "Success"), true, null);
    }
}
