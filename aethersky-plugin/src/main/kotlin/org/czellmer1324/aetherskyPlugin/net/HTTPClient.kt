package org.czellmer1324.aetherskyPlugin.net

import com.czellmer1324.dto.PlayerData
import com.google.gson.Gson
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.czellmer1324.aetherskyPlugin.AetherskyPlugin
import org.czellmer1324.aetherskyPlugin.player.ServerPlayer
import org.czellmer1324.aetherskyPlugin.player.pre.join.PreJoinPlayerInfo
import java.util.*

object HTTPClient {
    private const val URL = "http://mastercontrol:8081/"
    private val gson = Gson()
    private lateinit var plugin: AetherskyPlugin
    private val client = HttpClient {
        expectSuccess = true

        install(HttpRequestRetry) {
            retryOnServerErrors(maxRetries = 5)
            delayMillis { retry ->
                retry * 250L
            }
        }
    }

    fun init(plugin: AetherskyPlugin) {
        this.plugin = plugin
        this.plugin.logger.info("HTTP client initialized")
    }

    fun shutDown() {
        plugin.logger.info("HTTP client shutting down")
        client.close()
    }

    suspend fun retrievePlayerInfo(uuid: UUID): PreJoinPlayerInfo {
        val response = client.get(URL + "player/" + uuid)
        val playerInfo = withContext(Dispatchers.Default) {
            gson.fromJson(response.body<String>(), PreJoinPlayerInfo::class.java)
        }

        return playerInfo
    }

    suspend fun storePlayer(player: ServerPlayer) {
        val data = PlayerData(player.uuid)
        val dataJson = gson.toJson(data)

        client.put(URL + "player/store") {
            contentType(ContentType.Application.Json)
            setBody(dataJson)
        }
    }
}