package org.czellmer1324.aetherskyPlugin

import org.bukkit.plugin.java.JavaPlugin
import org.czellmer1324.aetherskyPlugin.listeners.PlayerJoinAndLeave
import org.czellmer1324.aetherskyPlugin.net.HTTPClient

class AetherskyPlugin : JavaPlugin() {

    override fun onEnable() {

        // Plugin startup logic
        HTTPClient.init(this)
        server.pluginManager.registerEvents(PlayerJoinAndLeave(this), this)
    }

    override fun onDisable() {
        // Plugin shutdown logic
        // ensure that all requests are finished before cancelling the scope to prevent data loss
    }
}
