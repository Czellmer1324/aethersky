package org.czellmer1324.proxyPlugin.redis

import com.google.common.base.Predicate
import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.sync.RedisCommands
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.reactive.asFlow

object RedisConnectionManager : AutoCloseable {
    private val redisClient: RedisClient = RedisClient.create("redis://aetherRedis:6379")
    private val connection: StatefulRedisConnection<String, String> = redisClient.connect()
    private val pubSubConnection: StatefulRedisPubSubConnection<String, String> = redisClient.connectPubSub()
    private val commands: RedisCommands<String?, String?>? = connection.sync()

    private val messageFlow = RedisConnectionManager.pubSubConnection.reactive()
        .observeChannels()
        .asFlow()

    fun reactiveSubscribe(channel : String) {
        pubSubConnection.reactive().subscribe(channel).subscribe()
    }

    suspend fun waitForMessage(channel: String, predicate: (String) -> Boolean) : String {
        return messageFlow
            .first { it.channel == channel && predicate(it.message) }
            .message
    }


    override fun close() {
        connection.close()
        redisClient.shutdown()
    }
}