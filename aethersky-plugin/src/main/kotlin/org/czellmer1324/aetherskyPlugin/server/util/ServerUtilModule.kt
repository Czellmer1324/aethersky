package org.czellmer1324.aetherskyPlugin.server.util

import com.github.shynixn.mccoroutine.bukkit.registerSuspendingEvents
import org.czellmer1324.aetherskyPlugin.AetherskyPlugin
import org.czellmer1324.aetherskyPlugin.player.listeners.PlayerJoinAndLeave
import org.czellmer1324.aetherskyPlugin.player.listeners.ServerMoveActionDeny
import org.czellmer1324.aetherskyPlugin.player.pre.join.PlayerPreJoinHandler
import org.czellmer1324.aetherskyPlugin.server.util.net.HTTPClient
import org.czellmer1324.aetherskyPlugin.server.util.redis.PlayerMovePubSub

// For enabling things that are used across every server

object ServerUtilModule {
    fun init(plugin: AetherskyPlugin) {
        PlayerMovePubSub.init(plugin)
        PlayerPreJoinHandler.init(plugin)
        HTTPClient.init(plugin)

        // EVENTS
        plugin.server.pluginManager.registerSuspendingEvents(PlayerJoinAndLeave(plugin), plugin)
        plugin.server.pluginManager.registerEvents(ServerMoveActionDeny(), plugin)
    }
}