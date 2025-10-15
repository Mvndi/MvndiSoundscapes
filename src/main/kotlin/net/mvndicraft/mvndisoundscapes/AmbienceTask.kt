package net.mvndicraft.mvndisoundscapes

import fr.formiko.mc.biomeutils.NMSBiomeUtils
import org.bukkit.Material
import org.bukkit.SoundCategory
import org.bukkit.entity.Player
import kotlin.math.absoluteValue

class AmbienceTask(private val plugin: MvndiSoundscapes) {

    private var ambience = mapOf(
        "mvndi:is_snowy" to "mvndicraft:soundscapes.ambient.surface.weather.snowy",
        "mvndi:is_plains" to "mvndicraft:soundscapes.ambient.surface.plains",
        "minecraft:is_forest" to "mvndicraft:soundscapes.ambient.surface.forest.day_loop",
        "mvndi:is_desert" to "mvndicraft:soundscapes.ambient.surface.desert.day_loop",
        "minecraft:is_ocean" to "mvndicraft:soundscapes.ambient.surface.sea.loop",
        "mvndi:is_swamp" to "mvndicraft:soundscapes.ambient.surface.swamp.day_loop",
        "minecraft:is_river" to "mvndicraft:soundscapes.ambient.surface.river.loop",
        "minecraft:is_beach" to "mvndicraft:soundscapes.ambient.surface.beach.loop",
    )

    private var nightAmbience = mapOf(
        "minecraft:is_forest" to "mvndicraft:soundscapes.ambient.surface.forest.night_loop",
        "mvndi:is_desert" to "mvndicraft:soundscapes.ambient.surface.desert.night_loop",
        "mvndi:is_swamp" to "mvndicraft:soundscapes.ambient.surface.swamp.night_loop",
    )

    fun run(player: Player) {
        val uuid = player.uniqueId
        val lastAmbient = plugin.lastAmbient

        if (lastAmbient.containsKey(uuid) && System.currentTimeMillis() - lastAmbient[uuid]!! < MvndiSoundscapes.AMBIENCE_DELAY) return

        if (player.location.y < 20 && MvndiSoundscapes.blockCount(player.location, 8, Material.AIR) >= 16) {
            val rand = (Math.random() * ((4) + 1)).toInt()
            if (rand == 0) player.playSound(
                player, "mvndicraft:soundscapes.ambient.caves.loop1", SoundCategory.AMBIENT, 0.05f, 0.15f
            )
            if (rand == 1) player.playSound(
                player, "mvndicraft:soundscapes.ambient.caves.loop2", SoundCategory.AMBIENT, 0.05f, 1.0f
            )
            if (rand == 2) player.playSound(
                player, "mvndicraft:soundscapes.ambient.caves.loop3", SoundCategory.AMBIENT, 0.05f, 1.0f
            )
            if (rand == 3) player.playSound(
                player, "mvndicraft:soundscapes.ambient.caves.loop4", SoundCategory.AMBIENT, 0.05f, 1.0f
            )
            return
        }

        val time = player.world.time
        val day = time in 1..12299
        val biomeKey = NMSBiomeUtils.getBiomeKeyString(player.location) ?: return
        for (tag in ambience.keys) {
            if (!NMSBiomeUtils.matchTag(biomeKey, tag)) continue

            player.playSound(
                player,
                if (!day && tag in nightAmbience) nightAmbience[tag]!! else ambience[tag]!!,
                SoundCategory.AMBIENT,
                getVolume(false, player),
                1.0f
            )
            lastAmbient[uuid] = System.currentTimeMillis()
            break
        }
    }

    companion object {
        private fun lerp(a: Float, b: Float, f: Float): Float {
            return a + f * (b - a)
        }

        // get volume based on y
        fun getVolume(wind: Boolean, player: Player): Float {
            val y = player.location.y
            if (y < 100 && wind) return 0.0f

            val f = lerp(0.0f, 1.0f, (y.toFloat().absoluteValue / 256))
            if (wind) return String.format("%.1f", f).toFloat()
            return 1.0f - String.format("%.1f", f).toFloat()
        }
    }
}