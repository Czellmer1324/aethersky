package org.czellmer1324.aetherskyPlugin.redis

import io.lettuce.core.pubsub.RedisPubSubAdapter
import org.czellmer1324.aetherskyPlugin.AetherskyPlugin
import org.slf4j.Logger
import java.util.concurrent.CountDownLatch

class MessageListener(val plugin: AetherskyPlugin) : RedisPubSubAdapter<String, String>() {
    var latch : CountDownLatch = CountDownLatch(1)

    var messagesReceived: List<String> = emptyList()
    override fun message(channel: String?, message: String?) {
        plugin.logger.info("Received message: $message from channel: $channel")
        messagesReceived = messagesReceived.plus(message!!)
        latch.countDown()
    }
}