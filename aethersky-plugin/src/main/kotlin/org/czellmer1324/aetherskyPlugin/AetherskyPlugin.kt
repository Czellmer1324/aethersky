package org.czellmer1324.aetherskyPlugin

import com.github.shynixn.mccoroutine.bukkit.registerSuspendingEvents
import dev.rollczi.litecommands.LiteCommands
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import org.czellmer1324.aetherskyPlugin.player.listeners.PlayerJoinAndLeave
import org.czellmer1324.aetherskyPlugin.net.HTTPClient
import org.czellmer1324.aetherskyPlugin.player.commands.island.IslandCommand
import org.czellmer1324.aetherskyPlugin.redis.PlayerMovePubSub

//TODO: Make this a suspending plugin
class AetherskyPlugin : JavaPlugin() {

    override fun onEnable() {
        // Plugin startup logic
        PlayerMovePubSub.init(this)
        HTTPClient.init(this)
        server.pluginManager.registerSuspendingEvents(PlayerJoinAndLeave(this), this)

        // For registering commands TODO: Separate this to a module possibly to make certain commands enabled or disabled based on server type
        this.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { it ->
            IslandCommand.register(it.registrar())
        }
    }

    override fun onDisable() {
        // Plugin shutdown logic
        // ensure that all requests are finished before cancelling the scope to prevent data loss
        HTTPClient.shutDown()
    }
}
