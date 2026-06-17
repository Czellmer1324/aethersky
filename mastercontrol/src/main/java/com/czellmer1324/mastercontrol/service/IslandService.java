package com.czellmer1324.mastercontrol.service;

import com.czellmer1324.mastercontrol.entity.IslandWorld;
import com.czellmer1324.mastercontrol.master.dto.ServiceResponse;
import com.czellmer1324.mastercontrol.repository.IslandWorldRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class IslandService {
    private static final String DEFAULT_ISLAND_ID = "DEFAULT_ISLAND_WORLD";
    // TODO: Add caching after basic read from database logic is there
    // TODO: Can do the default island using the asp plugin command within the game to save the file
    private final IslandWorldRepository worldRepository;

    public ServiceResponse getIsland(UUID ownerId) {
        Optional<IslandWorld> opWorld = worldRepository.findByOwnerId(ownerId);
        IslandWorld world;

        if (opWorld.isEmpty()) {
            // Fall back to default world blob
            log.info("We made it here");
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

        return new ServiceResponse(world.getWorldData(), true, "No Fail");
    }

    private boolean islandExists(String islandId) {
        return worldRepository.existsById(islandId);
    }
}
