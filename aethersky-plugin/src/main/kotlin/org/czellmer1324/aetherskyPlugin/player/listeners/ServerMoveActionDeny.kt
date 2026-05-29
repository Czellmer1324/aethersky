package org.czellmer1324.aetherskyPlugin.player.listeners

import io.papermc.paper.event.player.PlayerPickItemEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.inventory.InventoryInteractEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerMoveEvent
import java.util.UUID

class ServerMoveActionDeny : Listener {
    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        val id = event.player.uniqueId

        if (contains(id)) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onPlayerDropItem(event: PlayerDropItemEvent) {
        val id = event.player.uniqueId

        if (contains(id)) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onInventoryOpen(event: InventoryOpenEvent) {
        val id = event.player.uniqueId

        if (contains(id)) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onCommand(event: PlayerCommandPreprocessEvent) {
        val id = event.player.uniqueId

        if (contains(id)) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onBlockPlace(event: BlockPlaceEvent) {
        val id = event.player.uniqueId

        if (contains(id)) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        val id = event.player.uniqueId

        if (contains(id)) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onInventoryInteract(event: InventoryInteractEvent) {
        val id = event.whoClicked.uniqueId

        if (contains(id)) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onBlockPickUp(event: PlayerPickItemEvent) {
        val id = event.player.uniqueId

        if (contains(id)) {
            event.isCancelled = true
        }
    }

    companion object {
        private val players: HashSet<UUID> = HashSet()

        fun add(id : UUID) {
            players.add(id)
        }

        fun remove(id : UUID) {
            players.remove(id)
        }

        private fun contains(id: UUID) : Boolean {
            return players.contains(id)
        }
    }
}