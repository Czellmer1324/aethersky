package org.czellmer1324.aetherskyPlugin.net

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import org.czellmer1324.aetherskyPlugin.AetherskyPlugin
import java.util.UUID

object HTTPClient {
    private const val URL = "http://mastercontrol:8081/"
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

    suspend fun retrievePlayerInfo(uuid: UUID) : String {
        val response = client.get(URL + "player/" + uuid)
        return response.body()
    }
}