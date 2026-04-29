package org.czellmer1324.aetherskyPlugin.player.pre.join

import java.util.UUID

object PreJoinCache {
    private val cache = HashMap<UUID, PreJoinPlayerInfo>()

    fun cachePreInfo(info: PreJoinPlayerInfo) {
        cache[info.uuid] = info
    }

    fun retrieveCachedInfo(uuid: UUID) : PreJoinPlayerInfo? {
        return cache.remove(uuid)
    }

    fun clearCache() {
        cache.clear()
    }
}