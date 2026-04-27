package com.czellmer1324.mastercontrol.controller;

import com.czellmer1324.dto.PlayerData;
import com.czellmer1324.mastercontrol.service.MasterPlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import requestObjects.PlayerDataRequest;

import java.util.UUID;

@RestController
@RequestMapping("/player")
@RequiredArgsConstructor
public class MasterPlayerController {
    private final MasterPlayerService service;

    @GetMapping("/{uuid}")
    public PlayerData getPlayer(@PathVariable UUID uuid) {
        return service.getPlayer(uuid);
    }
}
