package org.czellmer1324.aetherskyPlugin.hub

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.czellmer1324.aetherskyPlugin.AetherskyPlugin
import org.czellmer1324.aetherskyPlugin.player.commands.island.IslandCommand
import org.czellmer1324.aetherskyPlugin.server.util.ServerType

object HubModule {
    fun init(plugin: AetherskyPlugin) {
        if (plugin.serverInfo.serverType != ServerType.HUB) return

        // Commands that can be used on island
        plugin.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) {command ->
            IslandCommand.register(command.registrar(), plugin)
        }
    }
}