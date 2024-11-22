package net.mvndicraft.mvndisoundscapes.listeners

import net.mvndicraft.mvndisoundscapes.MvndiSoundscapes.moved
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent

class AetherListener : Listener {

    @EventHandler
    fun onPlayerFirstMove(event: PlayerMoveEvent) {
        val player = event.player
        val uuid = player.uniqueId
        if (uuid !in moved) {
            moved.add(uuid)
        }
    }
}