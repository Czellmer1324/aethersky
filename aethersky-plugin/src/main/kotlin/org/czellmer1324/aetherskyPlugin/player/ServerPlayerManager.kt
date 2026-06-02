package org.czellmer1324.aetherskyPlugin.player

import java.util.UUID

object ServerPlayerManager {
    private val cache = HashMap<UUID, ServerPlayer>()

    fun cachePlayer(player: ServerPlayer) {
        cache[player.uuid as UUID] = player
    }

    fun contains(uuid: UUID) : Boolean {
        return cache.containsKey(uuid)
    }

    fun getPlayer(uuid: UUID) : ServerPlayer? {
        return cache[uuid]
    }

    fun removePlayer(uuid: UUID): ServerPlayer {
        return cache.remove(uuid)!!
    }

    fun printPlayers() {
        if (cache.isEmpty()) {
            println("no current players")
        } else {
            cache.forEach { (uUID, player) ->
                println("$uUID    $player")
            }
        }
    }
}