package net.mvndicraft.mvndisoundscapes

import net.mvndicraft.mvndiseasons.biomes.NMSBiomeUtils
import org.bukkit.SoundCategory
import org.bukkit.entity.Player
import kotlin.math.absoluteValue

class AmbienceTask(private val plugin: MvndiSoundscapes) {

    fun run(player: Player) {
        val uuid = player.uniqueId
        val delay = 8000L
        val lastAmbient = plugin.lastAmbient

        if (lastAmbient.containsKey(uuid) && System.currentTimeMillis() - lastAmbient[uuid]!! < delay) return

        val time = player.world.time
        val day = (time in 12000..22999)
        val lower = (time in 12000..12500) || (time in 13000..22999)

        var playedAmbient = false

        val biomeKey = NMSBiomeUtils.getBiomeKeyString(player.location)

        val volume = getVolume(false, player)

        if (NMSBiomeUtils.matchTag(biomeKey, "minecraft:is_plains")) {
            player.playSound(
                player, "mvndicraft:soundscapes.ambient.surface.plains", SoundCategory.AMBIENT, volume, 1.0f
            )
            playedAmbient = true
        } else if (NMSBiomeUtils.matchTag(biomeKey, "minecraft:is_forest") || NMSBiomeUtils.matchTag(
                biomeKey, "mvndi:central_europe"
            )
        ) {
            if (day) {
                player.playSound(
                    player,
                    "mvndicraft:soundscapes.ambient.surface.forest.day_loop",
                    SoundCategory.AMBIENT,
                    if (lower) 0.15f * volume else 1.2f * volume,
                    1.0f
                )
            } else {
                player.playSound(
                    player,
                    "mvndicraft:soundscapes.ambient.surface.forest.night_loop",
                    SoundCategory.AMBIENT,
                    volume,
                    1.0f
                )
            }
            playedAmbient = true
        } else if (NMSBiomeUtils.matchTag(biomeKey, "mvndi:is_desert")) {
            if (day) {
                player.playSound(
                    player,
                    "mvndicraft:soundscapes.ambient.surface.desert.day_loop",
                    SoundCategory.AMBIENT,
                    if (lower) 0.15f * volume else 1.2f * volume,
                    1.0f
                )
            } else {
                player.playSound(
                    player,
                    "mvndicraft:soundscapes.ambient.surface.desert.night_loop",
                    SoundCategory.AMBIENT,
                    0.2f,
                    1.0f
                )
            }
            playedAmbient = true
        } else if (NMSBiomeUtils.matchTag(biomeKey, "minecraft:is_ocean") || NMSBiomeUtils.matchTag(
                biomeKey, "mvndi:is_deep_ocean"
            )
        ) {
            player.playSound(
                player,
                "mvndicraft:soundscapes.ambient.surface.sea.loop",
                SoundCategory.AMBIENT,
                if (lower) 0.15f * volume else 1.2f * volume,
                1.0f
            )
            playedAmbient = true
        } else if (NMSBiomeUtils.matchTag(biomeKey, "mvndi:is_snowy")) {
            if (player.world.hasStorm()) player.playSound(
                player, "mvndicraft:soundscapes.ambient.surface.weather.snowy", SoundCategory.AMBIENT, volume, 1.0f
            )
            playedAmbient = true
        } else if (NMSBiomeUtils.matchTag(biomeKey, "minecraft:is_beach")) {
            player.playSound(
                player,
                "mvndicraft:soundscapes.ambient.surface.beach.loop",
                SoundCategory.AMBIENT,
                if (lower) 0.1f * volume else 0.15f * volume,
                1.0f
            )
            playedAmbient = true
        } else if (NMSBiomeUtils.matchTag(biomeKey, "mvndi:is_swamp")) {
            if (day) {
                player.playSound(
                    player,
                    "mvndicraft:soundscapes.ambient.surface.swamp.day_loop",
                    SoundCategory.AMBIENT,
                    if (lower) 0.15f * volume else 0.2f * volume,
                    1.0f
                )
            } else {
                player.playSound(
                    player,
                    "mvndicraft:soundscapes.ambient.surface.swamp.night_loop",
                    SoundCategory.AMBIENT,
                    volume,
                    1.0f
                )
            }
            playedAmbient = true
        } else if (NMSBiomeUtils.matchTag(biomeKey, "minecraft:is_river")) {
            player.playSound(
                player, "mvndicraft:soundscapes.ambient.surface.river.loop", SoundCategory.AMBIENT, 1.0f, 1.0f
            )
            playedAmbient = true
        } else if (player.location.y < 20 && MvndiSoundscapes.airCount(player.location, 8) >= 64) {
            val rand = (Math.random() * ((4) + 1)).toInt()
            if (rand == 0) player.playSound(
                player, "mvndicraft:soundscapes.ambient.caves.loop1", SoundCategory.AMBIENT, volume, 1.0f
            )
            if (rand == 1) player.playSound(
                player, "mvndicraft:soundscapes.ambient.caves.loop2", SoundCategory.AMBIENT, volume, 1.0f
            )
            if (rand == 2) player.playSound(
                player, "mvndicraft:soundscapes.ambient.caves.loop3", SoundCategory.AMBIENT, 1.0f, 1.0f
            )
            if (rand == 3) player.playSound(
                player, "mvndicraft:soundscapes.ambient.caves.loop4", SoundCategory.AMBIENT, 1.0f, 1.0f
            )
            playedAmbient = true
        }
        if (playedAmbient) lastAmbient[uuid] = System.currentTimeMillis()
    }

    companion object {
        private fun lerp(a: Float, b: Float, f: Float): Float {
            return a + f * (b - a)
        }

        // get volume based on y
        public fun getVolume(wind: Boolean, player: Player): Float {
            val y = player.location.y
            if (y < 100 && wind) return 0.0f

            val f = lerp(0.0f, 1.0f, (y.toFloat().absoluteValue / 256))
            if (wind) return String.format("%.1f", f).toFloat()
            return 1.0f - String.format("%.1f", f).toFloat()
        }
    }
}