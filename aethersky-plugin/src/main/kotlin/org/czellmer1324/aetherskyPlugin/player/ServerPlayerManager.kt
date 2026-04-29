package org.czellmer1324.aetherskyPlugin.player

import java.util.UUID

object ServerPlayerManager {
    private val cache = HashMap<UUID, ServerPlayer>()

    fun cachePlayer(player: ServerPlayer) {
        cache[player.uuid as UUID] = player
    }

    fun getPlayer(uuid: UUID) : ServerPlayer? {
        return cache[uuid]
    }

    fun removePlayer(uuid: UUID) {
        cache.remove(uuid)
    }

    fun printPlayers() {
        cache.forEach { (uUID, player) ->
            println("$uUID    $player")
        }
    }
}