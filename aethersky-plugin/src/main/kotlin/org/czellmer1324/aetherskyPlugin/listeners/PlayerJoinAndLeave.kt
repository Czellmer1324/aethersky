package org.czellmer1324.aetherskyPlugin.listeners

import com.github.shynixn.mccoroutine.bukkit.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
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
    fun preLoginEvent(event: AsyncPlayerPreLoginEvent) {
        val job = plugin.launch(Dispatchers.IO) {
            plugin.logger.info("Pre join player event fir ")
            // Get the player info from master control
            val response = HTTPClient.retrievePlayerInfo(event.uniqueId)
            plugin.logger.info(response.toString())
            PreJoinCache.cachePreInfo(response)
        }

        // Wait for the player info to be retrieved and cached in preJoin cache
        runBlocking { job.join() }
    }

    @EventHandler
    fun playerJoinEvent(event: PlayerJoinEvent) {
        val info = PreJoinCache.retrieveCachedInfo(event.player.uniqueId)
        //TODO: NEED TO ADD HANDLING IF CACHING FAILS
        val sPlayer = ServerPlayer(info!!.uuid)
        ServerPlayerManager.cachePlayer(sPlayer)
    }

    @EventHandler
    fun playerLeaveEvent(ev: PlayerQuitEvent) {
        plugin.launch(Dispatchers.IO) {
            val player = ServerPlayerManager.removePlayer(ev.player.uniqueId)
            HTTPClient.storePlayer(player)
            //TODO: NEED TO PUSH UPDATED PLAYER INFO TO MASTERCONTROL
        }
    }
}