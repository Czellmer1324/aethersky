package org.czellmer1324.aetherskyPlugin.events

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.czellmer1324.aetherskyPlugin.AetherskyPlugin
import org.czellmer1324.aetherskyPlugin.net.HTTPClient

class JoinEvent(val plugin: AetherskyPlugin) : Listener {

    @EventHandler
    fun playerJoinEvent(event: PlayerJoinEvent) {
        val player = event.player
        plugin.coroutineScope.launch(Dispatchers.IO) {
            HTTPClient.retrievePlayerInfo(player.uniqueId)
        }
    }
}