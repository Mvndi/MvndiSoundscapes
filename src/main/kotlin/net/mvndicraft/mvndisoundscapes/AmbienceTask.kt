package net.mvndicraft.mvndisoundscapes

import net.mvndicraft.mvndiseasons.biomes.NMSBiomeUtils
import org.bukkit.Bukkit
import org.bukkit.SoundCategory
import org.bukkit.entity.Player
import kotlin.math.absoluteValue
import kotlin.math.truncate

class AmbienceTask(private val plugin: MvndiSoundscapes) {

    public fun run(player: Player) {
        val to = player.location
        val uuid = player.uniqueId
        val delay = 8000L
        val lastAmbient = plugin.lastAmbient

        if (lastAmbient.containsKey(uuid) && System.currentTimeMillis() - lastAmbient[uuid]!! < delay)
            return

        var mvndiBiomeName = ""
        if (Bukkit.getPluginManager().isPluginEnabled("MvndiSeasons")) {
            mvndiBiomeName = NMSBiomeUtils.getBiomeKeyString(player.location)
        }

        val time = player.world.time
        val day = (time in 12000..22999)
        val lower = (time in 12000..12500) || (time in 13000..22999)

        var biomeName = to.block.biome.key.value()
        var ns = to.block.biome.key.namespace
        if (ns.lowercase() == "minecraft")
            biomeName = mvndiBiomeName
        biomeName = biomeName.uppercase()
        var playedAmbient = false

        val volume = getVolume(false, player)

        if (biomeName.contains("PLAINS")) {
            player.playSound(
                player, "mvndicraft:soundscapes.ambient.surface.plains", SoundCategory.AMBIENT, volume, 1.0f
            )
            playedAmbient = true
        } else if (biomeName.contains("FOREST")) {
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
        } else if (biomeName.contains("DESERT")) {
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
        } else if (biomeName.contains("DESERT")) {
            player.playSound(
                player,
                "mvndicraft:soundscapes.ambient.surface.sea.loop",
                SoundCategory.AMBIENT,
                if (lower) 0.15f * volume else 1.2f * volume,
                1.0f
            )
            playedAmbient = true
        } else if (biomeName.contains("SNOW")) {
            if (player.world.hasStorm()) player.playSound(
                player, "mvndicraft:soundscapes.ambient.surface.weather.snowy", SoundCategory.AMBIENT, volume, 1.0f
            )
            playedAmbient = true
        } else if (biomeName.contains("BEACH")) {
            player.playSound(
                player,
                "mvndicraft:soundscapes.ambient.surface.beach.loop",
                SoundCategory.AMBIENT,
                if (lower) 0.1f * volume else 0.15f * volume,
                1.0f
            )
            playedAmbient = true
        } else if (biomeName.contains("SWAMP")) {
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
                    player, "mvndicraft:soundscapes.ambient.surface.swamp.night_loop", SoundCategory.AMBIENT, volume, 1.0f
                )
            }
            playedAmbient = true
        } else if (biomeName.contains("RIVER")) {
            player.playSound(
                player, "mvndicraft:soundscapes.ambient.surface.river.loop", SoundCategory.AMBIENT, 1.0f, 1.0f
            )
            playedAmbient = true
        } else if (biomeName.contains("CAVE")) {
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
        public fun getVolume(wind: Boolean, player: Player) : Float {
            val y = player.location.y
            if (y < 100 && wind)
                return 0.0f

            val f = lerp(0.0f, 1.0f, (y.toFloat().absoluteValue / 256))
            if (wind)
                return String.format("%.1f", f).toFloat()
            return 1.0f - String.format("%.1f", f).toFloat()
        }
    }
}