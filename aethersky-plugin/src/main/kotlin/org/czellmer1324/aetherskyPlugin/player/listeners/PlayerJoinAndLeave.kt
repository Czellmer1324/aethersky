package org.czellmer1324.aetherskyPlugin.player.listeners

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.czellmer1324.aetherskyPlugin.AetherskyPlugin
import org.czellmer1324.aetherskyPlugin.net.HTTPClient
import org.czellmer1324.aetherskyPlugin.player.ServerPlayer
import org.czellmer1324.aetherskyPlugin.player.ServerPlayerManager
import org.czellmer1324.aetherskyPlugin.player.pre.join.PreJoinCache

class PlayerJoinAndLeave(private val plugin: AetherskyPlugin) : Listener {


    @EventHandler
    suspend fun preLoginEvent(event: AsyncPlayerPreLoginEvent) {
        withContext(Dispatchers.IO) {
            // Get the player info from master control
            try {
                val response = HTTPClient.retrievePlayerInfo(event.uniqueId)
                plugin.logger.info(response.toString())
                PreJoinCache.cachePreInfo(response)
            } catch (e : Exception) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Component.text("Error retrieving data, please try again"))
                plugin.logger.warning("Error retrieving data for UUID:${event.uniqueId} while joining server")
                plugin.logger.warning(e.message)
            }
        }
    }

    @EventHandler
    fun playerJoinEvent(event: PlayerJoinEvent) {
        val info = PreJoinCache.retrieveCachedInfo(event.player.uniqueId)

        if (info == null) {
            event.player.kick(Component.text("Error retrieving data, try joining again")
                .color(TextColor.color(255, 0, 0)))
            return
        }

        val sPlayer = ServerPlayer(info.uuid, event.player)
        ServerPlayerManager.cachePlayer(sPlayer)
    }

    @EventHandler
    fun playerLeaveEvent(ev: PlayerQuitEvent) {
        // TODO: WILL NEED TO SAVE DATA FOR WHEN PLAYER LEAVES, however need to determine if it is a server move or a complete log off
        // Going to actually handle this on the proxy layer
    }
}