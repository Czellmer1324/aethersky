package org.czellmer1324.proxyPlugin

import com.czellmer1324.dto.RedisMessage
import com.github.shynixn.mccoroutine.velocity.launch
import com.velocitypowered.api.plugin.PluginContainer
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.proxy.server.RegisteredServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.czellmer1324.proxyPlugin.redis.RedisConnectionManager
import org.czellmer1324.proxyPlugin.redis.RedisPublisher
import java.util.Optional
import java.util.UUID

object PlayerTransfer {
    private const val MOVE_CHANNEL = "playerMove"
    private const val SUCCESS_CHANNEL = "successfulMove"
    private val redisPublisher = RedisPublisher()
    fun init(container: PluginContainer, servers: HashMap<String, RegisteredServer>, proxyServer: ProxyServer) {
        container.launch {
            // Listens for messages incoming on this channel to move players
            // Launches a new coroutine for each message to process the player movements
            RedisConnectionManager.listenToChannel(MOVE_CHANNEL).collect { message ->
                try {
                    container.launch(Dispatchers.Default) {
                        // Split the message
                        // In formate of - player:UUID:server:ServerName
                        val split = message.split(':')

                        // Grab server from the server list using the name from the message
                        println(split[3])
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
                        val response = actPlayer.createConnectionRequest(server).connect()

                        // Ensure the move was successful before sending the message that the move was completed
                        withContext(Dispatchers.IO) {
                            if (response.get().isSuccessful) {
                                sendMoveSuccess("player:$uuid")
                            }
                        }

                    }
                } catch (e : Exception) {
                    println("WARNING - Error transferring player to new server: ${e.message}")
                }
            }
        }
    }

    private fun sendMoveSuccess(message: String) {
        redisPublisher.publishMessage(SUCCESS_CHANNEL, RedisMessage(message))
    }
}