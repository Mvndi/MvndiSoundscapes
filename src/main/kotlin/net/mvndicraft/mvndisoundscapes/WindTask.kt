package net.mvndicraft.mvndisoundscapes

import org.bukkit.SoundCategory
import org.bukkit.entity.Player

class WindTask(private val plugin: MvndiSoundscapes) {
    public fun run(player: Player) {
        val lastWind = plugin.lastWind
        val delay = 8000L
        val to = player.location
        val uuid = player.uniqueId
        if (!lastWind.containsKey(uuid) || System.currentTimeMillis() - lastWind[uuid]!! >= delay) {
            val airCount = MvndiSoundscapes.airCount(to, 8)
            plugin.lastWind[uuid] = System.currentTimeMillis()
            val windVolume = AmbienceTask.getVolume(true, player)
            if (windVolume == 0f) return
            if (to.y >= 0 && airCount >= 128)
                player.playSound(
                    player, "mvndicraft:soundscapes.ambient.surface.wind.loop", SoundCategory.AMBIENT, windVolume, 1.0f
                )
            else {
                player.playSound(player, "mvndicraft:soundscapes.ambient.surface.wind.fadeout", SoundCategory.AMBIENT, windVolume, 1.0f)
            }
        }
    }
}