package org.czellmer1324.aetherskyPlugin.player.commands.island

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import kotlinx.coroutines.Dispatchers
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
import kotlin.time.Duration.Companion.seconds

object IslandCommand {
    lateinit var plugin: AetherskyPlugin
    private const val CHANNEL = "successfulMove"

    fun register(registrar: Commands, plugin: AetherskyPlugin) {
        this.plugin = plugin
        registrar.register(createCommand())
        ReactiveRedisConnectionManager.reactiveSubscribe(CHANNEL)
    }

    private fun createCommand() : LiteralCommandNode<CommandSourceStack> {
        // sub commands for /is
        val tp = Commands.literal("tp")
            .executes { teleportToIsland(it) }

        return Commands.literal("is")
            .then(tp)
            .build()
    }

    private fun teleportToIsland(ctx: CommandContext<CommandSourceStack>): Int {
        val sender = ctx.source.sender

        // Check to make sure the sender is a player
        if (sender !is Player) {
            sender.sendRichMessage("This command can only be used by players!")
            return Command.SINGLE_SUCCESS
        }

        // Grab the players UUID
        val id = sender.uniqueId

        sender.sendMessage { Component.text("Transferring servers").color(TextColor.color(0, 255, 0)) }
        // Prevent actions during the data sync process
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
                PlayerMovePubSub.sendReadyToMove("player:${player.uuid}:server:islands")

                // Wait for message from the proxy that the player has been moved
                withTimeout(5.seconds) {
                    ReactiveRedisConnectionManager.waitForMessage(CHANNEL) {it.startsWith("player:$id")}
                }

                // Ensure the player is no longer on the server before removing them from the player manager
                if (plugin.server.getPlayer(id) == null) {
                    ServerPlayerManager.removePlayer(id)
                } else {
                    throw PlayerTransferException("Player is still on the server!")
                }

            } catch (e : Exception) {
                sender.sendMessage { Component.text("There was an error transferring servers, please try again").color(
                    TextColor.color(255, 0, 0)) }
                plugin.logger.warning("Error transferring player: ${e.message}")
            }

            // remove the player from the action denier
            ServerMoveActionDeny.remove(sender.uniqueId)
        }

        return Command.SINGLE_SUCCESS
    }
}