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
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.entity.Player
import org.czellmer1324.aetherskyPlugin.AetherskyPlugin
import org.czellmer1324.aetherskyPlugin.net.HTTPClient
import org.czellmer1324.aetherskyPlugin.player.ServerPlayerManager
import org.czellmer1324.aetherskyPlugin.player.listeners.ServerMoveActionDeny
import org.czellmer1324.aetherskyPlugin.redis.PlayerMovePubSub

object IslandCommand {
    lateinit var plugin: AetherskyPlugin

    fun register(registrar: Commands, plugin: AetherskyPlugin) {
        this.plugin = plugin
        registrar.register(createCommand())
    }

    private fun createCommand() : LiteralCommandNode<CommandSourceStack> {
        val tp = Commands.literal("tp")
            .executes { doWork(it) }

        return Commands.literal("is")
            .then(tp)
            .build()
    }

    private fun doWork(ctx: CommandContext<CommandSourceStack>): Int {
        val sender = ctx.source.sender
        if (sender !is Player) {
            sender.sendRichMessage("This command can only be used by players!")
            return Command.SINGLE_SUCCESS
        }

        val id = sender.uniqueId

        sender.sendMessage { Component.text("Transferring servers").color(TextColor.color(0, 255, 0)) }
        ServerMoveActionDeny.add(id)

        plugin.launch(Dispatchers.IO) {
            try {
                val player = ServerPlayerManager.getPlayer(id) ?: throw ClassNotFoundException("Player was not found")
                val response = HTTPClient.storePlayer(player)

                if (response.status != HttpStatusCode.OK) {
                   throw ServerResponseException(response, response.bodyAsText())
                }

                PlayerMovePubSub.sendReadyToMove("player:${player.uuid}:server:islands")
            } catch (e : Exception) {
                sender.sendMessage { Component.text("There was an error transferring servers, please try again").color(
                    TextColor.color(255, 0, 0)) }
            }

            ServerMoveActionDeny.remove(sender.uniqueId)
        }

        return Command.SINGLE_SUCCESS
    }
}