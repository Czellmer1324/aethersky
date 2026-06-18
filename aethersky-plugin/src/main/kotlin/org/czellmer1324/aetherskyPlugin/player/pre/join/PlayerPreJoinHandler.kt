package org.czellmer1324.aetherskyPlugin.player.pre.join

import com.github.shynixn.mccoroutine.bukkit.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.czellmer1324.aetherskyPlugin.AetherskyPlugin
import org.czellmer1324.aetherskyPlugin.island.loader.IslandWorldLoader
import org.czellmer1324.aetherskyPlugin.server.util.ServerType
import org.czellmer1324.aetherskyPlugin.server.util.net.HTTPClient
import org.czellmer1324.aetherskyPlugin.server.util.redis.PlayerMovePubSub
import org.czellmer1324.aetherskyPlugin.server.util.redis.ReactiveRedisConnectionManager
import java.util.UUID

object PlayerPreJoinHandler {
    private const val CHANNEL = "preConnect"
    private const val JOIN_SUFFIX = "status:ready"
    private lateinit var plugin: AetherskyPlugin
    private lateinit var serverName : String
    private lateinit var serverType: ServerType

    fun init(plugin: AetherskyPlugin) {
        this.plugin = plugin
        serverName = plugin.serverInfo.serverName
        serverType = plugin.serverInfo.serverType
        startListener()
    }

    private fun startListener() {
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
                            PreJoinCache.cachePreInfo(response)

                            // TODO: Will need to do a lot more logic with when it comes to checking if the world is already loaded or not
                            // Will most likely need to create a world manager to check this info
                            if (serverType == ServerType.ISLAND) {
                                moveToIsland(id)
                            }

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

    private suspend fun moveToIsland(id: UUID) {
        IslandWorldLoader.loadWorld(id)
    }
}