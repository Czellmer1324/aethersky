package org.czellmer1324.aetherskyPlugin

import com.github.shynixn.mccoroutine.bukkit.registerSuspendingEvents
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.plugin.java.JavaPlugin
import org.czellmer1324.aetherskyPlugin.net.HTTPClient
import org.czellmer1324.aetherskyPlugin.player.commands.island.IslandCommand
import org.czellmer1324.aetherskyPlugin.player.listeners.PlayerJoinAndLeave
import org.czellmer1324.aetherskyPlugin.player.listeners.ServerMoveActionDeny
import org.czellmer1324.aetherskyPlugin.redis.PlayerMovePubSub


//TODO: Make this a suspending plugin
class AetherskyPlugin : JavaPlugin() {

    override fun onEnable() {
        // Plugin startup logic
        PlayerMovePubSub.init(this)
        HTTPClient.init(this)
        server.pluginManager.registerSuspendingEvents(PlayerJoinAndLeave(this), this)
        server.pluginManager.registerEvents(ServerMoveActionDeny(), this)
        this.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) {command ->
            IslandCommand.register(command.registrar(), this)
        }
    }

    override fun onDisable() {
        // Plugin shutdown logic
        // ensure that all requests are finished before cancelling the scope to prevent data loss
        HTTPClient.shutDown()
    }
}
