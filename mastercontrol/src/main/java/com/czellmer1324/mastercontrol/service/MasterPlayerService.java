package com.czellmer1324.mastercontrol.service;

import com.czellmer1324.dto.PlayerData;
import com.czellmer1324.mastercontrol.entity.MasterPlayer;
import com.czellmer1324.mastercontrol.master.dto.ServiceResponse;
import com.czellmer1324.mastercontrol.repository.MasterPlayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MasterPlayerService {
    private final MasterPlayerRepository playerRepository;

    public ServiceResponse getPlayer(UUID uuid) {
        Optional<MasterPlayer> opPlayer = playerRepository.findById(uuid);
        MasterPlayer player;
        // create a player for them and return it. Eventually will cache the player and then send the info
        player = opPlayer.orElseGet(() -> new MasterPlayer(uuid));

        PlayerData data = new PlayerData(player.getUuid());
        log.info(data.uuid().toString());
        return new ServiceResponse(data, true, "no fail");
    }

}
