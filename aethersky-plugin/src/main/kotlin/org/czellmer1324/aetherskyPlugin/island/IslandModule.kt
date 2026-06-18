package org.czellmer1324.aetherskyPlugin.island

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.Bukkit
import org.czellmer1324.aetherskyPlugin.AetherskyPlugin
import org.czellmer1324.aetherskyPlugin.island.loader.IslandWorldLoader
import org.czellmer1324.aetherskyPlugin.player.commands.TransferToHub
import org.czellmer1324.aetherskyPlugin.server.util.ServerType

// Used to register and start up all things island related
// Will only enable items if plugin is running on an island server

object IslandModule {
    fun init(plugin: AetherskyPlugin) {
        if (plugin.serverInfo.serverType != ServerType.ISLAND) return

        // Enable Features
        IslandWorldManager.init(plugin)
        IslandWorldLoader.init(plugin)

        // Commands that can be used on island
        plugin.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) {command ->
            TransferToHub.register(command.registrar(), plugin)
        }

    }
}