package org.czellmer1324.aetherskyPlugin.player.listeners

import com.github.shynixn.mccoroutine.bukkit.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
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

    //TODO: Make this a suspending listener OR Better option to is to use the pub/sub message
    @EventHandler
    fun preLoginEvent(event: AsyncPlayerPreLoginEvent) {
        val job = plugin.launch(Dispatchers.IO) {
            plugin.logger.info("Pre join player event fired ")
            // Get the player info from master control
            //TODO: Add a time out to this
            val response = HTTPClient.retrievePlayerInfo(event.uniqueId)
            plugin.logger.info(response.toString())
            PreJoinCache.cachePreInfo(response)

            // Send message to velocity proxy via pub/sub that this player is now ready to join the world
        }

        // Wait for the player info to be retrieved and cached in preJoin cache
        runBlocking { job.join() }
    }

    @EventHandler
    fun playerJoinEvent(event: PlayerJoinEvent) {
        val info = PreJoinCache.retrieveCachedInfo(event.player.uniqueId)
        //TODO: NEED TO ADD HANDLING IF CACHING FAILS
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
    }
}