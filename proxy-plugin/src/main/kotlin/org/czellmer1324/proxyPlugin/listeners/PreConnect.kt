package org.czellmer1324.proxyPlugin.listeners

import com.czellmer1324.dto.RedisMessage
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.player.ServerPreConnectEvent
import com.velocitypowered.api.proxy.server.RegisteredServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.czellmer1324.proxyPlugin.redis.RedisConnectionManager
import org.czellmer1324.proxyPlugin.redis.RedisPublisher
import org.slf4j.Logger

class PreConnect(val logger: Logger, private val servers: HashMap<String, RegisteredServer>) {
    private val channel = "preConnect"
    private val redisPublisher = RedisPublisher()

    init {
        RedisConnectionManager.reactiveSubscribe(channel)
    }


    // This fires before the disconnect event on the server
    @Subscribe
    suspend fun onServerPreconnect(event: ServerPreConnectEvent) {
        val id = event.player.uniqueId
        val targetServer = event.originalServer.serverInfo.name

        withContext(Dispatchers.IO) {
            // Send message through so server knows to pull data for the player
            redisPublisher.publishMessage(channel, RedisMessage("server:$targetServer:player:$id:status:pending_join"))
            logger.info("Waiting for message about id : $id")

            // wait for the message to come through
            val message = RedisConnectionManager.waitForMessage(channel) {it.startsWith("server:$targetServer:player:$id")}
            logger.info("received message : $message")

            // Need to check if the result is ready to move or failed
            val status = message.split(':').last()

            if (status == "failed") {
                // TODO: This may cause memory leak issues during server transfers. Need to somehow make sure player data on sub servers is cleaned up as well as if an island is loaded with no one on it
                event.player.disconnect(Component.text("There was an error retrieving your data, try joining again!").color(TextColor.color(255, 0, 0)))
            }
        }
    }



    // This fires when the player completely leaves the server network only not in between servers
    @Subscribe
    suspend fun onServerDisconnect(event: DisconnectEvent) {
        val loginStatus = event.loginStatus
        logger.info("Login status: $loginStatus")
        logger.info("Current server: ${event.player.currentServer}")
        logger.info(event.player.isActive.toString())
    }

}