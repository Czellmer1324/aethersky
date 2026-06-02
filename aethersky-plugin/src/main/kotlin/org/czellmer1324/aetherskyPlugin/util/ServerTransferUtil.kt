package org.czellmer1324.aetherskyPlugin.util

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.mojang.brigadier.context.CommandContext
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.papermc.paper.command.brigadier.CommandSourceStack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.entity.Player
import org.czellmer1324.aetherskyPlugin.AetherskyPlugin
import org.czellmer1324.aetherskyPlugin.exceptions.PlayerTransferException
import org.czellmer1324.aetherskyPlugin.net.HTTPClient
import org.czellmer1324.aetherskyPlugin.player.ServerPlayerManager
import org.czellmer1324.aetherskyPlugin.player.listeners.ServerMoveActionDeny
import org.czellmer1324.aetherskyPlugin.redis.PlayerMovePubSub
import org.czellmer1324.aetherskyPlugin.redis.ReactiveRedisConnectionManager
import java.util.UUID
import kotlin.text.startsWith
import kotlin.time.Duration.Companion.seconds

fun transferServer(plugin: AetherskyPlugin, ctx: CommandContext<CommandSourceStack>, targetServer: String) {
    val channel = "successfulMove"
    val sender = ctx.source.sender

    // Check to make sure the sender is a player
    if (sender !is Player) {
        sender.sendRichMessage("This command can only be used by players!")
        return
    }

    // Grab the players UUID
    val id = sender.uniqueId
    sender.sendMessage { Component.text("Transferring servers").color(TextColor.color(0, 255, 0)) }
    ServerPlayerManager.pendMovePlayer(id)

    // Prevent the player from completing actions during the move process
    ServerMoveActionDeny.add(id)

    plugin.launch(Dispatchers.IO) {
        try {
            // Grab the server player object from server player manager
            val player = ServerPlayerManager.getPlayer(id) ?: throw ClassNotFoundException("Player was not found")

            // Send the data to master control to cache the player
            // Wait for the response to come back before allowing the move
            val response = HTTPClient.storePlayer(player)

            // Check to make sure the response was success
            if (response.status != HttpStatusCode.OK) {
                throw ServerResponseException(response, response.bodyAsText())
            }

            // Send the ready to move message to the proxy to tell it to transfer the player to next server
            PlayerMovePubSub.sendReadyToMove("player:${player.uuid}:server:$targetServer")

            // Wait for message from the proxy that the player has been moved
            withTimeout(5.seconds) {
                ReactiveRedisConnectionManager.waitForMessage(channel) { it.startsWith("player:$id") }
            }

            // Ensure the player is no longer on the server before removing them from the player manager
            if (plugin.server.getPlayer(id) == null) {
                ServerPlayerManager.removePlayer(id)
            } else {
                throw PlayerTransferException("Player is still on the server!")
            }
        } catch (e: Exception) {
            withContext(plugin.minecraftDispatcher) {
                sender.sendMessage {
                    Component.text("There was an error transferring servers, please try again").color(
                        TextColor.color(255, 0, 0)
                    )
                }
                plugin.logger.warning("Error transferring player: ${e.message}")
                e.printStackTrace()
            }
        }

        // remove the player from the action denier
        ServerMoveActionDeny.remove(id)
        ServerPlayerManager.unPendMovePlayer(id)
    }
}