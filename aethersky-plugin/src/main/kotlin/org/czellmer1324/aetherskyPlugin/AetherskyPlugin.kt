package org.czellmer1324.aetherskyPlugin

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.bukkit.plugin.java.JavaPlugin
import org.czellmer1324.aetherskyPlugin.events.JoinEvent
import org.czellmer1324.aetherskyPlugin.net.HTTPClient

class AetherskyPlugin : JavaPlugin() {
    lateinit var coroutineScope: CoroutineScope


    override fun onEnable() {
        // Plugin startup logic
        coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
        HTTPClient.init(this)
        server.pluginManager.registerEvents(JoinEvent(this), this)
    }

    override fun onDisable() {
        // Plugin shutdown logic
        HTTPClient.shutDown()
        coroutineScope.cancel()
    }
}
