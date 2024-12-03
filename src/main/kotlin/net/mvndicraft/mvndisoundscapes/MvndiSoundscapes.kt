package net.mvndicraft.mvndisoundscapes

import com.gmail.goosius.siegewar.SiegeWarAPI
import net.mvndicraft.mvndibattle.BattleTracker
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
import kotlin.collections.HashSet

class MvndiSoundscapes : JavaPlugin(), Listener {
    val lastAmbient = ConcurrentHashMap<UUID, Long>()
    private val lastPlayed = ConcurrentHashMap<UUID, Long>()
    private val lastBattle = ConcurrentHashMap<UUID, Long>()
    val lastWind = ConcurrentHashMap<UUID, Long>()
    private val startedTasks = HashSet<UUID>()
    private var soundscapes = mapOf(
        "nile" to "mvndicraft:soundscapes.soundtrack.egypt",
        "arabian" to "mvndicraft:soundscapes.soundtrack.egypt",
        "greece" to "mvndicraft:soundscapes.soundtrack.greece",
        "italy" to "mvndicraft:soundscapes.soundtrack.italy",
        "mvndi:is_plains" to "mvndicraft:soundscapes.soundtrack.plains",
        "mvndi:central_europe" to "mvndicraft:soundscapes.soundtrack.germany",
        "minecraft:is_forest" to "mvndicraft:soundscapes.soundtrack.forest",
        "mvndi:is_desert" to "mvndicraft:soundscapes.soundtrack.desert",
        "mvndi:is_mountain" to "mvndicraft:soundscapes.soundtrack.mountain",
        "mvndi:is_hill" to "mvndicraft:soundscapes.soundtrack.mountains",
        "minecraft:is_ocean" to "mvndicraft:soundscapes.soundtrack.ocean",
        "mvndi:is_snowy" to "mvndicraft:soundscapes.soundtrack.snowy",
        "mvndi:mediterranean_coast" to "mvndicraft:soundscapes.soundtrack.greece",
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
        lastBattle.remove(uuid)
    }

    @EventHandler
    fun onPlayerFirstMove(event: PlayerMoveEvent) {
        val player = event.player
        val uuid = player.uniqueId
        val aether = player.location.world.name == "aether"

        if (startedTasks.contains(uuid))
            return

        startedTasks.add(uuid)

        player.scheduler.runAtFixedRate(this, {
            if (Bukkit.getPluginManager().isPluginEnabled("MvndiBattle") && BattleTracker.getInstance()
                    .isInBattle(player.uniqueId) && (!lastBattle.containsKey(uuid) || (System.currentTimeMillis() - lastBattle[uuid]!! >= 67000))
            ) {
                player.stopSound(SoundCategory.MUSIC)
                player.playSound(
                    player, "mvndicraft:soundscapes.soundtrack.battle", SoundCategory.MUSIC, 2.0f, 1.0f
                )
                lastBattle[uuid] = System.currentTimeMillis()
                return@runAtFixedRate
            }

            if (lastPlayed.containsKey(uuid) && System.currentTimeMillis() - lastPlayed[uuid]!! < if (aether) 40700 else 420000L) return@runAtFixedRate

            if (aether) {
                player.playSound(
                    player, "mvndicraft:soundscapes.soundtrack.spawn", SoundCategory.MUSIC, 2.0f, 1.0f
                )
                lastPlayed[uuid] = System.currentTimeMillis()
                return@runAtFixedRate
            }

            val biomeKey = NMSBiomeUtils.getBiomeKeyString(player.location)

            if (player.location.y < 20 && blockCount(player.location, 8, Material.AIR) >= 64) {
                player.playSound(
                    player, "mvndicraft:soundscapes.soundtrack.cave", SoundCategory.MUSIC, 2.0f, 1.0f
                )
                lastPlayed[uuid] = System.currentTimeMillis()
                return@runAtFixedRate
            }

            if (Bukkit.getPluginManager().isPluginEnabled("SiegeWar") && SiegeWarAPI.getSiege(player).isPresent) {
                player.playSound(
                    player, "mvndicraft:soundscapes.soundtrack.siege", SoundCategory.MUSIC, 2.0f, 1.0f
                )
                lastPlayed[uuid] = System.currentTimeMillis()
                return@runAtFixedRate
            }

            for (soundscape in soundscapes.keys) if (biomeKey != null) if (biomeKey.contains(soundscape) || NMSBiomeUtils.matchTag(
                    biomeKey, soundscape
                )
            ) {
                player.playSound(player, soundscapes[soundscape]!!, SoundCategory.MUSIC, 2.0f, 1.0f)
                lastPlayed[uuid] = System.currentTimeMillis()
                return@runAtFixedRate
            }
        }, null, 1L, 20L)
    }

    companion object {
        fun blockCount(loc: Location, radius: Int, type: Material): Int {
            var count = 0

            for (x in loc.blockX - radius..loc.blockX + radius) {
                for (y in loc.blockY - radius..loc.blockY + radius) {
                    for (z in loc.blockZ - radius..loc.blockZ + radius) {
                        if (loc.world.getBlockAt(x, y, z).type == type) count++
                    }
                }
            }

            return count
        }
    }
}