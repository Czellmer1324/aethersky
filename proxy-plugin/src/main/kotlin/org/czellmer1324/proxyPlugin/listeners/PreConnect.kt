package org.czellmer1324.proxyPlugin.listeners

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.player.KickedFromServerEvent
import com.velocitypowered.api.event.player.ServerPreConnectEvent
import com.velocitypowered.api.proxy.server.RegisteredServer
import net.kyori.adventure.text.Component
import org.czellmer1324.proxyPlugin.redis.RedisConnectionManager
import org.slf4j.Logger
import java.util.UUID

class PreConnect(val logger: Logger, private val servers: HashMap<String, RegisteredServer>) {
    // I need to have a message listener instance here subscribing to the player move channel
    private val channel = "playerMove"
    private val pendingMove = HashMap<UUID, RegisteredServer>()

    init {
        RedisConnectionManager.reactiveSubscribe(channel)
    }


    // This fires before the disconnect event on the server
    @Subscribe
    fun onServerPreconnect(event: ServerPreConnectEvent) {
        // This gets the server the player is currently on
        val currentServer = event.player.currentServer
    }



    // This fires when the player completely leaves the server network only not in between servers
    @Subscribe
    suspend fun onServerDisconnect(event: DisconnectEvent) {
        val loginStatus = event.loginStatus
        logger.info("Login status: $loginStatus")
        logger.info("Current server: ${event.player.currentServer}")
        logger.info(event.player.isActive.toString())
    }

    @Subscribe
    suspend fun onPlayerKickEvent(event: KickedFromServerEvent) {
        val reason = event.serverKickReason
        logger.info(reason.toString())
        logger.info(Component.text("Moving servers...").toString())
        if (reason == Component.text("Moving servers...")) {
            // wait for pub/sub message here
            val player = event.player
            logger.info("Waiting for message to move player")
            val message = RedisConnectionManager.waitForMessage(channel) {it.startsWith("player:${player.uniqueId}")}
            val parts = message.split(':')
            logger.info("Received the right message")
            player.createConnectionRequest(servers[parts.last()])
        }
    }

}