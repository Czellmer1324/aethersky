package org.czellmer1324.aetherskyPlugin

import com.github.shynixn.mccoroutine.bukkit.registerSuspendingEvents
import com.google.gson.Gson
import io.ktor.http.ContentType.Application.Json
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.plugin.java.JavaPlugin
import org.czellmer1324.aetherskyPlugin.net.HTTPClient
import org.czellmer1324.aetherskyPlugin.net.PlayerPreJoinHandler
import org.czellmer1324.aetherskyPlugin.player.commands.TransferToHub
import org.czellmer1324.aetherskyPlugin.player.commands.island.IslandCommand
import org.czellmer1324.aetherskyPlugin.player.listeners.PlayerJoinAndLeave
import org.czellmer1324.aetherskyPlugin.player.listeners.ServerMoveActionDeny
import org.czellmer1324.aetherskyPlugin.redis.PlayerMovePubSub
import org.czellmer1324.aetherskyPlugin.server.util.ServerInfo
import java.io.File


//TODO: Make this a suspending plugin
class AetherskyPlugin : JavaPlugin() {
    lateinit var serverInfo : ServerInfo

    override fun onEnable() {
        // Plugin startup logic
        // Get the server name from info.json
        val jsonInfo = File("./info.json").readText()
        serverInfo = Gson().fromJson(jsonInfo, ServerInfo::class.java)

        // Initiate objects/modules
        PlayerMovePubSub.init(this)
        PlayerPreJoinHandler.init(this)
        HTTPClient.init(this)
        server.pluginManager.registerSuspendingEvents(PlayerJoinAndLeave(this), this)
        server.pluginManager.registerEvents(ServerMoveActionDeny(), this)
        this.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) {command ->
            IslandCommand.register(command.registrar(), this)
            TransferToHub.register(command.registrar(), this)
        }
    }

    override fun onDisable() {
        // Plugin shutdown logic
        // ensure that all requests are finished before cancelling the scope to prevent data loss
        HTTPClient.shutDown()
    }
}
