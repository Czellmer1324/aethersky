package org.czellmer1324.aetherskyPlugin.net

import com.github.shynixn.mccoroutine.bukkit.launch
import kotlinx.coroutines.Dispatchers
import net.kyori.adventure.text.Component
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.czellmer1324.aetherskyPlugin.AetherskyPlugin
import org.czellmer1324.aetherskyPlugin.player.pre.join.PreJoinCache
import org.czellmer1324.aetherskyPlugin.redis.PlayerMovePubSub
import org.czellmer1324.aetherskyPlugin.redis.ReactiveRedisConnectionManager
import java.util.UUID

object PlayerPreJoinHandler {
    private const val CHANNEL = "preConnect"
    private const val JOIN_SUFFIX = "status:ready"
    private lateinit var serverName : String

    fun init(plugin: AetherskyPlugin) {
        serverName = plugin.serverInfo.serverName

        plugin.launch(Dispatchers.IO) {
            // Listen to messages from proxy
            ReactiveRedisConnectionManager.listenToChannel(CHANNEL).collect { message ->
                if (message.startsWith("server:$serverName") && message.endsWith("status:pending_join")) {
                    // Start new coroutine to handle grabbing the players info and caching it for join execution
                    plugin.launch(Dispatchers.IO) {
                        // Message content -- server:$targetServer:player:$playerUUID:status:pending_join
                        val split = message.split(':')
                        val id = UUID.fromString(split[3])
                        try {
                            val response = HTTPClient.retrievePlayerInfo(id)
                            plugin.logger.info(response.toString())
                            PreJoinCache.cachePreInfo(response)
                            PlayerMovePubSub.sendReadyToConnect("server:$serverName:player:$id:$JOIN_SUFFIX")
                        } catch (e : Exception) {
                            plugin.logger.warning("Error retrieving data for UUID:${id} while joining server")
                            plugin.logger.warning(e.message)
                            PlayerMovePubSub.sendReadyToConnect("server:$serverName:player:$id:status:failed")
                        }
                    }
                }
            }
        }
    }
}