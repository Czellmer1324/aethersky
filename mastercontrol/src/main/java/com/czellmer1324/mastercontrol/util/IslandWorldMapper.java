package com.czellmer1324.mastercontrol.util;

import com.czellmer1324.mastercontrol.entity.IslandWorld;
import com.czellmer1324.mastercontrol.entity.IslandWorldCache;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface IslandWorldMapper {
    IslandWorldCache toIslandWorldCache(IslandWorld islandWorld);
    IslandWorld toIslandWorld(IslandWorldCache islandWorldCache);
}
