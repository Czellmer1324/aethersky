package com.czellmer1324.mastercontrol.write.behind.workers;

import com.czellmer1324.mastercontrol.entity.IslandWorld;
import com.czellmer1324.mastercontrol.entity.IslandWorldCache;
import com.czellmer1324.mastercontrol.repository.IslandWorldRepository;
import com.czellmer1324.mastercontrol.util.IslandWorldMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

@Component
@AllArgsConstructor
@Slf4j
public class IslandWorldWriteTask {
    private final RedisTemplate<String, Object> redisTemplate;
    private final IslandWorldRepository islandWorldRepository;
    private final IslandWorldMapper mapper;
    private static final String SET_KEY = "island_worlds:pending_sync";

    // TODO: Make this have its own Hibernate worker pool at some point
    @Scheduled(initialDelay = 60, fixedRate = 5, timeUnit = TimeUnit.SECONDS)
    public void saveIslands() {
        if (redisTemplate.opsForSet().size(SET_KEY) == 0) {
            return;
        }

        log.info("Saving some islands via worker");
        // pull 2 to 3 islands from the pending set to store. After save remove them
        // pull the islands from the redis set for ones marked as dirty
        ArrayList<IslandWorld> islandsToSave = new ArrayList<>();

        try {
            for (int i = 0; i < 3; i++) {
                if (redisTemplate.opsForSet().size(SET_KEY) != 0) {
                    IslandWorldCache island = (IslandWorldCache) redisTemplate.opsForSet().pop(SET_KEY);
                    islandsToSave.add(mapper.toIslandWorld(island));
                }
            }

            if (!islandsToSave.isEmpty()) {
                islandWorldRepository.saveAll(islandsToSave);
            }

            log.info("{} worlds saved", islandsToSave.size());
        } catch (Exception e) {
            // re-add the islands back in if the update fails
            log.warn("Failed to save islands to the database");
            log.warn(e.getMessage());
            islandsToSave.forEach(islandWorld -> redisTemplate.opsForSet().add(SET_KEY, mapper.toIslandWorldCache(islandWorld)));
        }

        // convert them to the repository object from the redis object using the mapper
    }
}
