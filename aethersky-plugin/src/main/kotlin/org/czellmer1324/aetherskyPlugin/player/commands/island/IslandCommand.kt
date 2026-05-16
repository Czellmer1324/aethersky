package org.czellmer1324.aetherskyPlugin.player.commands.island

import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.entity.Player

object IslandCommand {
    // I want the main (Parent command to be /is)
    fun register(registrar: Commands) {
        // need the built command to register
        registrar.register(build())
    }

    private fun build() : LiteralCommandNode<CommandSourceStack> {
        // Children nodes

        //Go
        // Create a function for execution logic to go in
        val go = Commands.literal("go")
            .executes { islandGoLogic(it) }

        // This is the root node
        // use .then() to add child nodes
        return Commands.literal("is")
            .then(go)
            .build()
    }

    private fun islandGoLogic(ctx : CommandContext<CommandSourceStack>) : Int {
        // get the sender
        val sender = ctx.source.sender

        // check to make sure they are a player
        if (sender !is Player) {
            sender.sendRichMessage("Only players can use this command!")
            return Command.SINGLE_SUCCESS
        }

        // TP logic will go here
        sender.sendRichMessage("You are a player!")
        return Command.SINGLE_SUCCESS
    }
}