package com.czellmer1324.mastercontrol.repository;

import com.czellmer1324.mastercontrol.entity.IslandWorld;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface IslandWorldRepository extends CrudRepository<IslandWorld, UUID> {
}
