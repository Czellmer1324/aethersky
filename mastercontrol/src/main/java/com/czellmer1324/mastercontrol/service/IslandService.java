package com.czellmer1324.mastercontrol.service;

import com.czellmer1324.mastercontrol.entity.IslandWorld;
import com.czellmer1324.mastercontrol.master.dto.ServiceResponse;
import com.czellmer1324.mastercontrol.repository.IslandWorldRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Transactional
    public ServiceResponse saveIsland(UUID ownerId, byte[] worldData) {
        try {

            IslandWorld world = worldRepository.findById(ownerId).orElseThrow(() -> new EntityNotFoundException("World not found"));

            world.setWorldData(worldData);
        } catch (Exception e) {
            log.warn("Error saving island for user {}: {}", ownerId, e.getMessage());
            return new ServiceResponse(Map.of("Message", "Failed to save island"), false, "Failed to save");
        }

        return new ServiceResponse(Map.of("Message", "Success"), true, null);
    }
}
