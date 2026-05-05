package com.czellmer1324.mastercontrol.util;

import com.czellmer1324.mastercontrol.entity.MasterPlayer;
import com.czellmer1324.mastercontrol.entity.MasterPlayerCache;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MasterPlayerMapper {
    MasterPlayer toMasterPlayer(MasterPlayerCache cachePlayer);
    MasterPlayerCache toMasterPlayerCache(MasterPlayer masterPlayer);
}
