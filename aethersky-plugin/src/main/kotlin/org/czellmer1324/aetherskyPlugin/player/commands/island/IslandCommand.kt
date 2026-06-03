package org.czellmer1324.aetherskyPlugin.player.commands.island

import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.czellmer1324.aetherskyPlugin.AetherskyPlugin
import org.czellmer1324.aetherskyPlugin.player.commands.TransferToHub
import org.czellmer1324.aetherskyPlugin.player.util.transferServer

// TODO: WILL NEED TO MAKE SURE THIS WORKS WHEN THERE ARE MULTIPLE ISLAND SERVERS
// I.E. load balancing
object IslandCommand {
    lateinit var plugin: AetherskyPlugin
    private const val TARGET_MOVE_SERVER = "islands-1"

    fun register(registrar: Commands, plugin: AetherskyPlugin) {
        this.plugin = plugin
        registrar.register(createCommand())
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
        transferServer(TransferToHub.plugin, ctx, TARGET_MOVE_SERVER)

        return Command.SINGLE_SUCCESS
    }
}