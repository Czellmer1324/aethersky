package org.czellmer1324.aetherskyPlugin.server.util.redis

import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.sync.RedisCommands
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactive.asFlow

object ReactiveRedisConnectionManager : AutoCloseable {
    private val redisClient: RedisClient = RedisClient.create("redis://aetherRedis:6379")
    private val connection: StatefulRedisConnection<String, String> = redisClient.connect()
    private val pubSubConnection: StatefulRedisPubSubConnection<String, String> = redisClient.connectPubSub()
    private val commands: RedisCommands<String?, String?>? = connection.sync()

    fun init() {
        reactiveSubscribe("successfulMove")
    }

    private val messageFlow = pubSubConnection.reactive()
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

    fun listenToChannel(channel: String) : Flow<String> {
        pubSubConnection.reactive().subscribe(channel).subscribe()

        return messageFlow.filter { it.channel == channel }
            .map { it.message }
    }

    override fun close() {
        connection.close()
        redisClient.shutdown()
    }
}