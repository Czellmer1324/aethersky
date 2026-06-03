package org.czellmer1324.proxyPlugin.redis

import com.czellmer1324.dto.RedisMessage

class RedisPublisher {
    fun publishMessage(channel: String, message: RedisMessage) {
        RedisConnectionManager.redisSyncCommands()?.publish(channel, message.content())
    }
}