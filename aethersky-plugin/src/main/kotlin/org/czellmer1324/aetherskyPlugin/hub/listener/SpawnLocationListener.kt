package org.czellmer1324.aetherskyPlugin.hub.listener

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class SpawnLocationListener : Listener{
    // TODO: MAKE THESE CONFIGURABLE IN GAME WITH A COMMAND FROM AN ADMIN
    private val x : Double = 162.5
    private val y : Double = 67.0
    private val z : Double = 232.5
    private val pitch : Float = (-180.0).toFloat()
    private val yaw : Float = (0.0).toFloat()

    @EventHandler(priority = EventPriority.NORMAL)
    fun tpPlayerToHubSpawnLoc(ev : PlayerJoinEvent) {
        val player = ev.player
        if (Bukkit.getPlayer(player.uniqueId) == null) {
            return
        }

        player.teleport(Location(player.world, x, y, z, pitch, yaw))

    }
}