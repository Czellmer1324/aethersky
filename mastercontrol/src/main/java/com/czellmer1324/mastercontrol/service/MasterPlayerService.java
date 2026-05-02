package com.czellmer1324.mastercontrol.service;

import com.czellmer1324.dto.PlayerData;
import com.czellmer1324.mastercontrol.entity.MasterPlayer;
import com.czellmer1324.mastercontrol.master.dto.ServiceResponse;
import com.czellmer1324.mastercontrol.repository.MasterPlayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    //TODO: Implement redis cache for player info
    public ServiceResponse getPlayer(UUID uuid) {
        Optional<MasterPlayer> opPlayer = playerRepository.findById(uuid);
        MasterPlayer player;
        // create a player for them and return it. Eventually will cache the player and then send the info
        player = opPlayer.orElseGet(() -> new MasterPlayer(uuid));

        PlayerData data = new PlayerData(player.getUuid());
        log.info(data.uuid().toString());
        return new ServiceResponse(data, true, "no fail");
    }

    public ServiceResponse storePlayer(PlayerData data) {
        // Will save the info to cache here rather than update the database, for now update the database
        Optional<MasterPlayer> opPlayer = playerRepository.findById(data.uuid());
        try {
            if (opPlayer.isEmpty()) {
                playerRepository.save(new MasterPlayer(data.uuid()));
            } else {
                MasterPlayer player = opPlayer.get();
                player.updateInfo(data);
                playerRepository.save(player);
            }

            log.info("Saved player with uuid: {}", data.uuid());

            return new ServiceResponse(Map.of("Message", "Player stored"), true, "No fail");
        } catch (Exception e) {
            return new ServiceResponse(Map.of("Message", "Something went wrong"), false, "Failure saving to database");
        }
    }
}
