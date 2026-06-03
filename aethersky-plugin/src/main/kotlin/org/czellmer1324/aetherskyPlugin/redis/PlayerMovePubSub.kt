package org.czellmer1324.aetherskyPlugin.redis

import com.czellmer1324.dto.RedisMessage
import org.czellmer1324.aetherskyPlugin.AetherskyPlugin

object PlayerMovePubSub {
    private lateinit var plugin: AetherskyPlugin
    lateinit var messageListener: MessageListener
    lateinit var redisPublisher : RedisPublisher
    const val MOVE_CHANNEL = "playerMove"
    const val READY_TO_CONNECT_CHANNEL = "preConnect"

    fun init(plugin: AetherskyPlugin) {
        this.plugin = plugin
        messageListener = MessageListener(this.plugin)
        redisPublisher = RedisPublisher(this.plugin)

        // init reactive subscriber
        ReactiveRedisConnectionManager.init()
        this.plugin.logger.info("Player Pub/Sub messenger initialized")
    }

    fun sendReadyToMove(message: String) {
        redisPublisher.publishMessage(MOVE_CHANNEL, RedisMessage(message))
    }

    fun sendReadyToConnect(message: String) {
        redisPublisher.publishMessage(READY_TO_CONNECT_CHANNEL, RedisMessage(message))
    }
}