package org.czellmer1324.aetherskyPlugin.island

import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.infernalsuite.asp.api.AdvancedSlimePaperAPI
import com.infernalsuite.asp.api.world.SlimeWorldInstance
import kotlinx.coroutines.withContext
import org.czellmer1324.aetherskyPlugin.AetherskyPlugin
import org.czellmer1324.aetherskyPlugin.exceptions.IslandNotLoadedException
import org.czellmer1324.aetherskyPlugin.exceptions.PlayerNotCachedException
import org.czellmer1324.aetherskyPlugin.player.ServerPlayerManager
import java.util.UUID

object IslandWorldManager {
    // TODO: Store extra data here as well?
    private val loadedIslands = HashMap<String, SlimeWorldInstance>()
    private lateinit var plugin: AetherskyPlugin

    fun init(plugin: AetherskyPlugin) {
        this.plugin = plugin
    }

    fun addWorld(worldName: String, world: SlimeWorldInstance) {
        loadedIslands[worldName] = world
    }

    fun removeWorld(worldName: String) {
        loadedIslands.remove(worldName)
    }

    fun alreadyLoaded(worldName: String) : Boolean {
        return loadedIslands.containsKey(worldName)
    }

    fun saveWorld(worldName: String) {
        AdvancedSlimePaperAPI.instance().saveWorld(loadedIslands[worldName])
    }

    fun getWorld(worldName: String) : SlimeWorldInstance? {
        return loadedIslands[worldName]
    }

    @Throws(IslandNotLoadedException::class, PlayerNotCachedException::class)
    suspend fun movePlayerToIsland(worldName: String, playerId: UUID) {
        withContext(plugin.minecraftDispatcher) {
            val player = ServerPlayerManager.getPlayer(playerId) ?: throw PlayerNotCachedException("Player with id: $playerId is not cached within player manager.")
            val world = loadedIslands[worldName] ?: throw IslandNotLoadedException("Island is not loaded for ${player.uuid}")
            val bukkitWorld = world.bukkitWorld
            player.wrapped!!.teleport(bukkitWorld.spawnLocation)
        }
    }
}