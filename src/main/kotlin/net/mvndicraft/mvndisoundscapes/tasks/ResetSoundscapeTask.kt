package net.mvndicraft.mvndisoundscapes.tasks

import net.mvndicraft.mvndisoundscapes.MvndiSoundscapes
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType


class ResetSoundscapesTask : Runnable {
    override fun run() {
        Bukkit.getServer().onlinePlayers.parallelStream().forEach { player: Player? ->
            val pdc = player!!.persistentDataContainer
            val key: NamespacedKey = MvndiSoundscapes.WIND_KEY!!
            val key2: NamespacedKey = MvndiSoundscapes.AETHER_KEY!!
            if (pdc.has(key) && pdc.get(key, PersistentDataType.INTEGER) == 1) pdc.set(
                key, PersistentDataType.INTEGER, 0
            )
            if (pdc.has(key2) && pdc.get(key2, PersistentDataType.INTEGER) == 1) pdc.set(
                key2, PersistentDataType.INTEGER, 0
            )
        }
    }
}
