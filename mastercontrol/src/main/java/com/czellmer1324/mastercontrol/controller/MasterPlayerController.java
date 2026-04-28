package com.czellmer1324.mastercontrol.controller;

import com.czellmer1324.dto.PlayerData;
import com.czellmer1324.mastercontrol.master.dto.ServiceResponse;
import com.czellmer1324.mastercontrol.service.MasterPlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import requestObjects.PlayerDataRequest;

import java.util.UUID;

@RestController
@RequestMapping("/player")
@RequiredArgsConstructor
public class MasterPlayerController {
    private final MasterPlayerService service;

    @GetMapping("/{uuid}")
    public ResponseEntity<?> getPlayer(@PathVariable UUID uuid) {
        ServiceResponse responseInfo = service.getPlayer(uuid);
        HttpStatus code;

        if (responseInfo.successful()) {
            code = HttpStatus.OK;
        } else {
            code = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return ResponseEntity.status(code).body(responseInfo.response());
    }
}
