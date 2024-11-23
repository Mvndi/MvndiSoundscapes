package net.mvndicraft.mvndisoundscapes

import net.mvndicraft.mvndiseasons.biomes.NMSBiomeUtils
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.SoundCategory
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.plugin.java.JavaPlugin
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class MvndiSoundscapes : JavaPlugin(), Listener {
    private val delay = 420000L
    private val lastAmbient = ConcurrentHashMap<UUID, Long>()
    private val lastPlayed = ConcurrentHashMap<UUID, Long>()
    private val lastWind = ConcurrentHashMap<UUID, Long>()
    private var soundscapes = mapOf(
        "nile" to "mvndicraft:soundscapes.soundtrack.egypt",
        "arabian" to "mvndicraft:soundscapes.soundtrack.egypt",
        "greece" to "mvndicraft:soundscapes.soundtrack.greece",
        "italy" to "mvndicraft:soundscapes.soundtrack.italy",
        "germany" to "mvndicraft:soundscapes.soundtrack.germany"
    )

    override fun onEnable() {
        // Plugin startup logic
        logger.info("Enabling MvndiSoundscapes")
        Bukkit.getPluginManager().registerEvents(this, this)
    }

    override fun onDisable() {
        // Plugin shutdown logic
        logger.info("Disabling MvndiSoundscapes")
    }

    @EventHandler
    fun onPlayerFirstMove(event: PlayerMoveEvent) {
        val player = event.player
        val uuid = player.uniqueId
        val to = player.location

        if (!lastWind.containsKey(uuid) || (lastWind.containsKey(uuid) && System.currentTimeMillis() - lastWind[uuid]!! < delay)) {
            lastWind[uuid] = System.currentTimeMillis()
            if (to.y >= 128 && airCount(to, 8) >= 128)
                player.playSound(
                    player, "mvndicraft:soundscapes.ambient.surface.wind.loop", SoundCategory.AMBIENT, 0.5f, 1.0f
                )
        }

        if (lastPlayed.containsKey(uuid) && System.currentTimeMillis() - lastPlayed[uuid]!! < delay) return

        lastPlayed[uuid] = System.currentTimeMillis()

        if (player.location.world.name == "aether") {
            player.playSound(
                player, "mvndicraft:soundscapes.soundtrack.spawn", SoundCategory.MUSIC, 1.2f, 1.0f
            )
            return
        }

        var mvndiBiomeName = ""
        if (Bukkit.getPluginManager().isPluginEnabled("MvndiSeasons")) {
            mvndiBiomeName = NMSBiomeUtils.getBiomeKeyString(player.location)
            soundscapes.keys.parallelStream().forEach { soundscape ->
                if (mvndiBiomeName.contains(soundscape)) player.playSound(
                    player, soundscapes[soundscape]!!, SoundCategory.MUSIC, 0.5f, 1.0f
                )
            }
        }

        if (lastAmbient.containsKey(uuid) && System.currentTimeMillis() - lastAmbient[uuid]!! < delay) return

        val time = player.world.time
        val day = (time in 12000..22999)
        val lower = (time in 12000..12500) || (time in 13000..22999)

        var biomeName = to.block.biome.name
        if (biomeName.lowercase() == "custom")
            biomeName = mvndiBiomeName
        biomeName = biomeName.uppercase()
        var playedAmbient = false

        if (biomeName.contains("PLAINS")) {
            player.playSound(
                player, "mvndicraft:soundscapes.soundtrack.plains", SoundCategory.MUSIC, 1.2f, 1.0f
            )
            player.playSound(
                player, "mvndicraft:soundscapes.ambient.surface.plains", SoundCategory.AMBIENT, 0.5f, 1.0f
            )
            playedAmbient = true
        } else if (biomeName.contains("FOREST")) {
            player.playSound(
                player, "mvndicraft:soundscapes.soundtrack.forest", SoundCategory.MUSIC, 1.2f, 1.0f
            )
            if (day) {
                player.playSound(
                    player,
                    "mvndicraft:soundscapes.ambient.surface.forest.day_loop",
                    SoundCategory.AMBIENT,
                    if (lower) 0.15f else 1.2f,
                    1.0f
                )
            } else {
                player.playSound(
                    player,
                    "mvndicraft:soundscapes.ambient.surface.forest.night_loop",
                    SoundCategory.AMBIENT,
                    0.8f,
                    1.0f
                )
            }
            playedAmbient = true
        } else if (biomeName.contains("DESERT")) {
            player.playSound(
                player, "mvndicraft:soundscapes.soundtrack.desert", SoundCategory.MUSIC, 1.2f, 1.0f
            )
            if (day) {
                player.playSound(
                    player,
                    "mvndicraft:soundscapes.ambient.surface.desert.day_loop",
                    SoundCategory.AMBIENT,
                    if (lower) 0.15f else 1.2f,
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
                player, "mvndicraft:soundscapes.soundtrack.ocean", SoundCategory.MUSIC, 3.0f, 1.0f
            )
            player.playSound(
                player,
                "mvndicraft:soundscapes.ambient.surface.sea.loop",
                SoundCategory.AMBIENT,
                if (lower) 0.15f else 1.2f,
                1.0f
            )
            playedAmbient = true
        } else if (biomeName.contains("SNOW")) {
            player.playSound(
                player, "mvndicraft:soundscapes.soundtrack.snowy", SoundCategory.MUSIC, 1.2f, 1.0f
            )
            if (player.world.hasStorm()) player.playSound(
                player, "mvndicraft:soundscapes.ambient.surface.weather.snowy", SoundCategory.AMBIENT, 0.5f, 1.0f
            )
            playedAmbient = true
        } else if (biomeName.contains("BEACH")) {
            player.playSound(
                player,
                "mvndicraft:soundscapes.ambient.surface.beach.loop",
                SoundCategory.AMBIENT,
                if (lower) 0.1f else 0.15f,
                1.0f
            )
            playedAmbient = true
        } else if (biomeName.contains("SWAMP")) {
            if (day) {
                player.playSound(
                    player,
                    "mvndicraft:soundscapes.ambient.surface.swamp.day_loop",
                    SoundCategory.AMBIENT,
                    if (lower) 0.15f else 0.2f,
                    1.0f
                )
            } else {
                player.playSound(
                    player, "mvndicraft:soundscapes.ambient.surface.swamp.night_loop", SoundCategory.AMBIENT, 0.5f, 1.0f
                )
            }
            playedAmbient = true
        } else if (biomeName.contains("RIVER")) {
            player.playSound(
                player, "mvndicraft:soundscapes.ambient.surface.river.loop", SoundCategory.AMBIENT, 60f, 1.0f
            )
            playedAmbient = true
        } else if (biomeName.contains("CAVE")) {
            val rand = (Math.random() * ((4) + 1)).toInt()
            if (rand == 0) player.playSound(
                player, "mvndicraft:soundscapes.ambient.caves.loop1", SoundCategory.AMBIENT, 0.1f, 1.0f
            )
            if (rand == 1) player.playSound(
                player, "mvndicraft:soundscapes.ambient.caves.loop2", SoundCategory.AMBIENT, 0.2f, 1.0f
            )
            if (rand == 2) player.playSound(
                player, "mvndicraft:soundscapes.ambient.caves.loop3", SoundCategory.AMBIENT, 40f, 1.0f
            )
            if (rand == 3) player.playSound(
                player, "mvndicraft:soundscapes.ambient.caves.loop4", SoundCategory.AMBIENT, 60f, 1.0f
            )
            playedAmbient = true
        }

        if (playedAmbient) lastAmbient[uuid] = System.currentTimeMillis()
    }

    private fun airCount(loc: Location, radius: Int): Int {
        var count = 0

        for (x in loc.blockX - radius..loc.blockX + radius) {
            for (y in loc.blockY - radius..loc.blockY + radius) {
                for (z in loc.blockZ - radius..loc.blockZ + radius) {
                    if (loc.world.getBlockAt(x, y, z).type == Material.AIR) count++
                }
            }
        }

        return count
    }
}