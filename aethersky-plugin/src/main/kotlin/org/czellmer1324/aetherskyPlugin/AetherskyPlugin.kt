package org.czellmer1324.aetherskyPlugin

import com.google.gson.Gson
import org.bukkit.plugin.java.JavaPlugin
import org.czellmer1324.aetherskyPlugin.hub.HubModule
import org.czellmer1324.aetherskyPlugin.island.IslandModule
import org.czellmer1324.aetherskyPlugin.server.util.net.HTTPClient
import org.czellmer1324.aetherskyPlugin.server.util.ServerInfo
import org.czellmer1324.aetherskyPlugin.server.util.ServerUtilModule
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
        ServerUtilModule.init(this)
        IslandModule.init(this)
        HubModule.init(this)
    }

    override fun onDisable() {
        // Plugin shutdown logic
        // ensure that all requests are finished before cancelling the scope to prevent data loss
        HTTPClient.shutDown()
    }
}
