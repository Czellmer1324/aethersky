package org.czellmer1324.aetherskyPlugin.player.listeners

import com.github.shynixn.mccoroutine.bukkit.launch
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.czellmer1324.aetherskyPlugin.AetherskyPlugin
import org.czellmer1324.aetherskyPlugin.net.HTTPClient
import org.czellmer1324.aetherskyPlugin.player.ServerPlayer
import org.czellmer1324.aetherskyPlugin.player.ServerPlayerManager
import org.czellmer1324.aetherskyPlugin.player.pre.join.PreJoinCache
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

class PlayerJoinAndLeave(private val plugin: AetherskyPlugin) : Listener {


    @EventHandler
    suspend fun preLoginEvent(event: AsyncPlayerPreLoginEvent) {
        withContext(Dispatchers.IO) {
            // Get the player info from master control
            try {
                val response = HTTPClient.retrievePlayerInfo(event.uniqueId)
                plugin.logger.info(response.toString())
                PreJoinCache.cachePreInfo(response)
            } catch (e : Exception) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Component.text("Error retrieving data, please try again"))
                plugin.logger.warning("Error retrieving data for UUID:${event.uniqueId} while joining server")
                plugin.logger.warning(e.message)
            }
        }
    }

    @EventHandler
    fun playerJoinEvent(event: PlayerJoinEvent) {
        // Removes and returns the player info from the pre-join cache
        val info = PreJoinCache.retrieveCachedInfo(event.player.uniqueId)

        if (info == null) {
            event.player.kick(Component.text("Error retrieving data, try joining again")
                .color(TextColor.color(255, 0, 0)))
            return
        }

        val sPlayer = ServerPlayer(info.uuid, event.player)
        ServerPlayerManager.cachePlayer(sPlayer)
    }

    @EventHandler
    fun playerLeaveEvent(ev: PlayerQuitEvent) {
        val id = ev.player.uniqueId

        // Check to see if the server player manager contains the players info
        // If it doesn't that means they were transferred servers by one of the commands
        // If it does that means they are logging off, or something else happened and data still needs to be saved

        if (!ServerPlayerManager.contains(id)) return

        plugin.launch(Dispatchers.IO) {
            try {
                savePlayerOnLeave(id)
            } catch (e : Exception) {
                plugin.logger.warning("Error trying to save data for player ${ev.player.name} : ${e.message}")

                // Delay the coroutine for 3 seconds before retrying to save data again
                delay(3.seconds)
                retrySave(id)
            }
        }
    }

    private suspend fun savePlayerOnLeave(id : UUID) {
        val result = HTTPClient.storePlayer(ServerPlayerManager.getPlayer(id)!!)

        if (result.status != HttpStatusCode.OK) {
            throw ServerResponseException(result, result.bodyAsText())
        }

        // Remove them from cache after we have ensured data save works correctly
        ServerPlayerManager.removePlayer(id)
    }

    private suspend fun retrySave(id : UUID) {
        try {
            savePlayerOnLeave(id)
        } catch (e : Exception) {
            plugin.logger.warning("Unable to save data for $id on retry : ${e.message}")
        }
    }
}