package org.czellmer1324.aetherskyPlugin.net

import com.czellmer1324.dto.PlayerData
import com.google.gson.Gson
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.czellmer1324.aetherskyPlugin.AetherskyPlugin
import org.czellmer1324.aetherskyPlugin.player.ServerPlayer
import org.czellmer1324.aetherskyPlugin.player.pre.join.PreJoinPlayerInfo
import java.util.UUID

object HTTPClient {
    private const val URL = "http://mastercontrol:8081/"
    private val gson = Gson()
    private var plugin: AetherskyPlugin ? = null
    private val client = HttpClient {
        expectSuccess = true
    }

    fun init(plugin: AetherskyPlugin) {
        this.plugin = plugin
        this.plugin!!.logger.info("HTTP client initialized")
    }

    fun shutDown() {
        this.plugin!!.logger.info("HTTP client shutting down")
        client.close()
    }

    //TODO: Handle retrying the call if it fails to get player info
    suspend fun retrievePlayerInfo(uuid: UUID) : PreJoinPlayerInfo {
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