package org.czellmer1324.aetherskyPlugin.listeners

import com.github.shynixn.mccoroutine.bukkit.launch
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
import org.czellmer1324.aetherskyPlugin.player.pre.join.PreJoinPlayerInfo

class PlayerJoinAndLeave(private val plugin: AetherskyPlugin) : Listener {

    @EventHandler
    fun preLoginEvent(event: AsyncPlayerPreLoginEvent) {
        plugin.launch(Dispatchers.IO) {
            val response = HTTPClient.retrievePlayerInfo(event.uniqueId)
            withContext(Dispatchers.Default) {
                val gson = Gson()
                val playerInfo = gson.fromJson(response, PreJoinPlayerInfo::class.java)
                PreJoinCache.cachePreInfo(playerInfo)
            }
        }
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
        plugin.launch(Dispatchers.Default) {
            ServerPlayerManager.removePlayer(ev.player.uniqueId)
            //TODO: NEED TO PUSH UPDATED PLAYER INFO TO MASTERCONTROL
        }
    }
}