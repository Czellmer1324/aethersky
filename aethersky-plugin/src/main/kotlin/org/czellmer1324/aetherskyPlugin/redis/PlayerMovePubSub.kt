package org.czellmer1324.aetherskyPlugin.redis

import com.czellmer1324.dto.RedisMessage
import org.czellmer1324.aetherskyPlugin.AetherskyPlugin

object PlayerMovePubSub {
    private lateinit var plugin: AetherskyPlugin
    lateinit var messageListener: MessageListener
    lateinit var redisPublisher : RedisPublisher
    const val CHANNEL = "playerMove"

    fun init(plugin: AetherskyPlugin) {
        this.plugin = plugin
        messageListener = MessageListener(this.plugin)
        redisPublisher = RedisPublisher(this.plugin)
        this.plugin.logger.info("Player Pub/Sub messenger initialized")
    }

    fun sendReadyToMove(message: String) {
        redisPublisher.publishMessage(CHANNEL, RedisMessage(message))
    }
}