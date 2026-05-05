package com.czellmer1324.mastercontrol.repository;

import com.czellmer1324.mastercontrol.entity.MasterPlayerCache;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MasterPlayerRedis extends CrudRepository<MasterPlayerCache, UUID> {

}
