package org.czellmer1324.aetherskyPlugin.player.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.czellmer1324.aetherskyPlugin.AetherskyPlugin
import org.czellmer1324.aetherskyPlugin.player.util.transferServer

// TODO: WILL NEED TO MAKE SURE THIS WORKS WITH MULTIPLE HUB SERVERS UP
object TransferToHub {
    lateinit var plugin: AetherskyPlugin
    private const val TARGET_MOVE_SERVER = "hub"

    fun register(registrar: Commands, plugin: AetherskyPlugin) {
        this.plugin = plugin
        registrar.register(createCommand())
    }

    private fun createCommand() : LiteralCommandNode<CommandSourceStack> {

        return Commands.literal("hub")
            .executes { teleportToHub(it) }
            .build()
    }

    private fun teleportToHub(ctx: CommandContext<CommandSourceStack>): Int {
        transferServer(plugin, ctx, TARGET_MOVE_SERVER)

        return Command.SINGLE_SUCCESS
    }
}