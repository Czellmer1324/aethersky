package org.czellmer1324.aetherskyPlugin.player.listeners

import com.github.shynixn.mccoroutine.bukkit.launch
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
        // does nothing at the moment
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

        // Check to see if the player is pending server transfer
        // If they are, data does not need to be saved here

        if (ServerPlayerManager.checkPendingMove(id)) return

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