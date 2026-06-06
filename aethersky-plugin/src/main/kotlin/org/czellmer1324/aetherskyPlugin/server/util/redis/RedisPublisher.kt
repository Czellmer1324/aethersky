package org.czellmer1324.aetherskyPlugin.server.util.redis

import com.czellmer1324.dto.RedisMessage
import org.czellmer1324.aetherskyPlugin.AetherskyPlugin

class RedisPublisher(val plugin: AetherskyPlugin) {

    fun publishMessage(channel: String, message: RedisMessage) {
        RedisConnectionManager.redisSyncCommands()?.publish(channel, message.content())
        plugin.logger.info("Message published: $message")
    }
}