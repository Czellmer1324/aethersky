package com.czellmer1324.mastercontrol.controller;

import com.czellmer1324.mastercontrol.master.dto.ServiceResponse;
import com.czellmer1324.mastercontrol.service.IslandService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/islands")
@AllArgsConstructor
public class IslandController {
    private final IslandService islandService;

    /*
    Used for retrieving list of all island ID's that are currently stored within the postgres db
     */
    @GetMapping()
    public ResponseEntity<?> getIslandIds() {

        return ResponseEntity.status(200).body("");
    }

    /*
    Used for retrieving a specific world blob for an island
     */
    @GetMapping("/{uuid}")
    public ResponseEntity<?> getIsland(@PathVariable UUID uuid) {
        // will need to retrieve the world blob from the database
        // If the player does not already have an island, send them the default world
        ServiceResponse data = islandService.getIsland(uuid);
        if (data.successful()) {
            return ResponseEntity.status(200).body(data.response());
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(data.response());
        }
        // Logic here for after it getting island blob
    }

    /*
    Used to check if an island exists in the database
     */
    @GetMapping("/exists/{uuid}")
    public ResponseEntity<?> islandExists(@PathVariable UUID uuid) {

        return ResponseEntity.status(200).body("");
    }


    /*
    Used for saving a new world byte blob to the database for a given ID
     */
    @PostMapping("/{uuid}")
    public ResponseEntity<?> saveIsland(@PathVariable UUID uuid, @RequestBody byte[] world) {

        return ResponseEntity.status(200).body("");
    }
}
