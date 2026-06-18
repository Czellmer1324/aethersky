package org.czellmer1324.aetherskyPlugin.island.loader

import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.infernalsuite.asp.api.AdvancedSlimePaperAPI
import com.infernalsuite.asp.api.world.properties.SlimeProperties
import com.infernalsuite.asp.api.world.properties.SlimePropertyMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.czellmer1324.aetherskyPlugin.AetherskyPlugin
import org.czellmer1324.aetherskyPlugin.island.IslandWorldManager
import java.util.UUID

object IslandWorldLoader {
    private lateinit var plugin: AetherskyPlugin
    private val asp = AdvancedSlimePaperAPI.instance()
    private val loader = CustomApiLoader("http://mastercontrol:8081/islands/", "", "", true)
    private val propertyMap = SlimePropertyMap()

    fun init(plugin: AetherskyPlugin) {
        this.plugin = plugin

        // property map properties
        propertyMap.setValue(SlimeProperties.DIFFICULTY, "normal")
        propertyMap.setValue(SlimeProperties.ALLOW_ANIMALS, false)
        propertyMap.setValue(SlimeProperties.ALLOW_MONSTERS, false)
        propertyMap.setValue(SlimeProperties.PVP, false)
        propertyMap.setValue(SlimeProperties.ENVIRONMENT, "normal")
    }

    // TODO: Will need to add some type of world manager
    suspend fun loadWorld(playerId : UUID) {
        withContext(Dispatchers.IO) {

            if (!IslandWorldManager.alreadyLoaded(playerId.toString())) {
                val island = asp.readWorld(loader, playerId.toString(), false, propertyMap)

                // Loading the world needs to be done synchronously
                withContext(plugin.minecraftDispatcher) {
                    val worldInstance = asp.loadWorld(island, true)
                    IslandWorldManager.addWorld(playerId.toString(), worldInstance)
                    plugin.logger.info("Instance that I tried to load: ${worldInstance.name}:${worldInstance.bukkitWorld}")
                }
            }
        }
    }
}