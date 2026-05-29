package org.czellmer1324.proxyPlugin

import com.github.shynixn.mccoroutine.velocity.launch
import com.velocitypowered.api.plugin.PluginContainer
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.proxy.server.RegisteredServer
import kotlinx.coroutines.Dispatchers
import org.czellmer1324.proxyPlugin.redis.RedisConnectionManager
import java.util.Optional
import java.util.UUID

object PlayerTransfer {
    private const val CHANNEL = "playerMove"
    fun init(container: PluginContainer, servers: HashMap<String, RegisteredServer>, proxyServer: ProxyServer) {
        container.launch {
            // Listens for messages incoming on this channel to move players
            // Launches a new coroutine for each message to process the player movements
            RedisConnectionManager.listenToChannel(CHANNEL).collect { message ->
                container.launch(Dispatchers.Default) {
                    servers.forEach { (string, server) ->
                        println("name: $string, ${server.serverInfo}")
                    }
                    // Split the message
                    // In formate of - player:UUID:server:ServerName
                    val split = message.split(':')

                    // Grab server from the server list using the name from the message
                    val server = servers[split[3]]

                    // Check to make sure the server is not null
                    if (server == null) {
                        println("Something happening here")
                        return@launch
                    }

                    // Get the uuid from the message
                    val uuid : UUID = UUID.fromString(split[1])
                    // Get the player
                    val player : Optional<Player> = proxyServer.getPlayer(uuid)

                    // Make sure the player is not empty
                    if (player.isEmpty) {
                        println("Something happening at player")
                        return@launch
                    }

                    val actPlayer = player.get()

                    // Create the connection request for the player
                    actPlayer.createConnectionRequest(server).connect()
                }
            }
        }
    }
}