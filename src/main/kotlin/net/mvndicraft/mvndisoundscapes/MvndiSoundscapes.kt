package net.mvndicraft.mvndisoundscapes

import com.gmail.goosius.siegewar.SiegeWarAPI
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
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class MvndiSoundscapes : JavaPlugin(), Listener {
    val lastAmbient = ConcurrentHashMap<UUID, Long>()
    private val lastPlayed = ConcurrentHashMap<UUID, Long>()
    val lastWind = ConcurrentHashMap<UUID, Long>()
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
                    if (player.location.world.name != "aether") wind.run(player)
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
        val aether = player.location.world.name == "aether"

        if (lastPlayed.containsKey(uuid) && System.currentTimeMillis() - lastPlayed[uuid]!! < if (aether) 50000 else 420000L) return

        if (aether) {
            player.playSound(
                player, "mvndicraft:soundscapes.soundtrack.spawn", SoundCategory.MUSIC, 1.2f, 1.0f
            )
            lastPlayed[uuid] = System.currentTimeMillis()
            return
        }


        val mvndiBiomeName = NMSBiomeUtils.getBiomeKeyString(player.location)

        for (soundscape in soundscapes.keys) {
            if (mvndiBiomeName != null) {
                if (mvndiBiomeName.contains(soundscape)) {
                    player.playSound(player, soundscapes[soundscape]!!, SoundCategory.MUSIC, 0.5f, 1.0f)
                    lastPlayed[uuid] = System.currentTimeMillis()
                    return
                }
            }
        }

        val biomeKey = NMSBiomeUtils.getBiomeKeyString(player.location)
        var playedMusic = false

        if (Bukkit.getPluginManager().isPluginEnabled("SiegeWar") && SiegeWarAPI.getSiege(player).isPresent) {
            player.playSound(
                player, "mvndicraft:soundscapes.soundtrack.siege", SoundCategory.MUSIC, 1.0f, 1.0f
            )
            playedMusic = true
        } else if (NMSBiomeUtils.matchTag(biomeKey, "minecraft:is_plains")) {
            player.playSound(
                player, "mvndicraft:soundscapes.soundtrack.plains", SoundCategory.MUSIC, 1.2f, 1.0f
            )
            playedMusic = true
        } else if (NMSBiomeUtils.matchTag(biomeKey, "minecraft:is_forest") || NMSBiomeUtils.matchTag(
                biomeKey, "mvndi:central_europe"
            )
        ) {
            player.playSound(
                player, "mvndicraft:soundscapes.soundtrack.forest", SoundCategory.MUSIC, 1.2f, 1.0f
            )
            playedMusic = true
        } else if (NMSBiomeUtils.matchTag(biomeKey, "mvndi:is_desert")) {
            player.playSound(
                player, "mvndicraft:soundscapes.soundtrack.desert", SoundCategory.MUSIC, 1.2f, 1.0f
            )
            playedMusic = true
        } else if (NMSBiomeUtils.matchTag(biomeKey, "minecraft:is_ocean") || NMSBiomeUtils.matchTag(
                biomeKey, "mvndi:is_deep_ocean"
            )
        ) {
            player.playSound(
                player, "mvndicraft:soundscapes.soundtrack.ocean", SoundCategory.MUSIC, 3.0f, 1.0f
            )
            playedMusic = true
        } else if (NMSBiomeUtils.matchTag(biomeKey, "mvndi:is_snowy")) {
            player.playSound(
                player, "mvndicraft:soundscapes.soundtrack.snowy", SoundCategory.MUSIC, 1.2f, 1.0f
            )
            playedMusic = true
        } else if (player.location.y < 20 && MvndiSoundscapes.blockCount(player.location, 8, Material.AIR) >= 64) {
            player.playSound(
                player, "mvndicraft:soundscapes.soundtrack.cave", SoundCategory.MUSIC, 1.0f, 1.0f
            )
            playedMusic = true
        }

        if (playedMusic) lastPlayed[uuid] = System.currentTimeMillis()
    }

    companion object {
        fun blockCount(loc: Location, radius: Int, mat: Material): Int {
            var count = 0

            for (x in loc.blockX - radius..loc.blockX + radius) {
                for (y in loc.blockY - radius..loc.blockY + radius) {
                    for (z in loc.blockZ - radius..loc.blockZ + radius) {
                        if (loc.world.getBlockAt(x, y, z).type == mat) count++
                    }
                }
            }

            return count
        }
    }
}