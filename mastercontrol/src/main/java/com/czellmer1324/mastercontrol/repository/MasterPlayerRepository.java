package com.czellmer1324.mastercontrol.repository;

import com.czellmer1324.mastercontrol.entity.MasterPlayer;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface MasterPlayerRepository extends CrudRepository<MasterPlayer, UUID> {
}
