package net.mvndicraft.mvndisoundscapes

import net.mvndicraft.mvndiseasons.biomes.NMSBiomeUtils
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.SoundCategory
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class MvndiSoundscapes : JavaPlugin(), Listener {
    val delay = 420000L
    val lastAmbient = ConcurrentHashMap<UUID, Long>()
    val lastPlayed = ConcurrentHashMap<UUID, Long>()
    val lastWind = ConcurrentHashMap<UUID, Long>()
    var soundscapes = mapOf(
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
        startTasks()
    }

    override fun onDisable() {
        // Plugin shutdown logic
        logger.info("Disabling MvndiSoundscapes")
    }

    private fun startTasks() {
        val ambience = AmbienceTask(this)
        val wind = WindTask(this)

        Bukkit.getGlobalRegionScheduler().runAtFixedRate(this, {
            Bukkit.getOnlinePlayers().forEach { player ->
                player.scheduler.run(this, {
                    ambience.run(player)
                    wind.run(player)
                }, null)
            }
        }, 1L, 1L)
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player
        val uuid = player.uniqueId
        lastAmbient.remove(uuid)
        lastPlayed.remove(uuid)
        lastWind.remove(uuid)
    }

    @EventHandler
    fun onPlayerFirstMove(event: PlayerMoveEvent) {
        val player = event.player
        val uuid = player.uniqueId
        val to = player.location


        if (lastPlayed.containsKey(uuid) && System.currentTimeMillis() - lastPlayed[uuid]!! < delay) return

        if (player.location.world.name == "aether") {
            player.playSound(
                player, "mvndicraft:soundscapes.soundtrack.spawn", SoundCategory.MUSIC, 1.2f, 1.0f
            )
            lastPlayed[uuid] = System.currentTimeMillis()
            return
        }

        var mvndiBiomeName = ""
        if (Bukkit.getPluginManager().isPluginEnabled("MvndiSeasons")) {
            mvndiBiomeName = NMSBiomeUtils.getBiomeKeyString(player.location)

            for (soundscape in soundscapes.keys) {
                if (mvndiBiomeName.contains(soundscape)) {
                    player.playSound(player, soundscapes[soundscape]!!, SoundCategory.MUSIC, 0.5f, 1.0f)
                    lastPlayed[uuid] = System.currentTimeMillis()
                    return
                }
            }
            return
        }


        var biomeName = to.block.biome.name
        if (biomeName.lowercase() == "custom")
            biomeName = mvndiBiomeName
        biomeName = biomeName.uppercase()
        var playedMusic = false;

        if (biomeName.contains("PLAINS")) {
            player.playSound(
                player, "mvndicraft:soundscapes.soundtrack.plains", SoundCategory.MUSIC, 1.2f, 1.0f
            )
            playedMusic = true
        } else if (biomeName.contains("FOREST")) {
            player.playSound(
                player, "mvndicraft:soundscapes.soundtrack.forest", SoundCategory.MUSIC, 1.2f, 1.0f
            )
            playedMusic = true
        } else if (biomeName.contains("DESERT")) {
            player.playSound(
                player, "mvndicraft:soundscapes.soundtrack.desert", SoundCategory.MUSIC, 1.2f, 1.0f
            )
            playedMusic = true
        } else if (biomeName.contains("DESERT")) {
            player.playSound(
                player, "mvndicraft:soundscapes.soundtrack.ocean", SoundCategory.MUSIC, 3.0f, 1.0f
            )
            playedMusic = true
        } else if (biomeName.contains("SNOW")) {
            player.playSound(
                player, "mvndicraft:soundscapes.soundtrack.snowy", SoundCategory.MUSIC, 1.2f, 1.0f
            )
            playedMusic = true
        }

        if (playedMusic) lastPlayed[uuid] = System.currentTimeMillis()
    }

    companion object {
        public fun airCount(loc: Location, radius: Int): Int {
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
}