package net.mvndicraft.mvndisoundscapes

import com.gmail.goosius.siegewar.SiegeWarAPI
import com.gmail.goosius.siegewar.objects.BattleSession
import com.palmergames.bukkit.towny.TownyAPI
import com.palmergames.bukkit.towny.`object`.Government
import fr.formiko.mc.biomeutils.NMSBiomeUtils
import net.jodah.expiringmap.ExpiringMap
import net.mvndicraft.mvndibattle.BattleTracker
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
import java.util.concurrent.TimeUnit

class MvndiSoundscapes : JavaPlugin(), Listener {
    companion object {
        const val MUSIC_DELAY = 420000L
        const val AMBIENCE_DELAY = 8000L

        fun blockCount(loc: Location, radius: Int, type: Material): Int {
            var count = 0
            for (x in loc.blockX - radius..loc.blockX + radius) for (y in loc.blockY - radius..loc.blockY + radius) for (z in loc.blockZ - radius..loc.blockZ + radius) if (loc.world.getBlockAt(
                    x, y, z
                ).type == type
            ) count++

            return count
        }

        val lastAmbient = ConcurrentHashMap<UUID, Long>()
        val lastPlayed = ConcurrentHashMap<UUID, Long>()
        val lastWind = ConcurrentHashMap<UUID, Long>()
        var playedSiegeEnd = false
        var playedSiegeBriefing = false
        private val lastBattle = ConcurrentHashMap<UUID, Long>()
        private val startedTasks = HashSet<UUID>()
        private val siegesPlayingAttackersAdvancing =
            ExpiringMap.builder().expiration(46, TimeUnit.SECONDS).build<UUID, Boolean>()
        private var soundscapes = mapOf(
            "minecraft:is_ocean" to "mvndicraft:music.battle.ocean_battle",

            "mvndi:bandit_arabian" to "mvndicraft:music.battle.arabic",

            "mvndi:bandit_western_european" to "mvndicraft:music.battle.european",
            "mvndi:bandit_slav" to "mvndicraft:music.battle.european",

            "mvndi:eastern_mediterranean_forest" to "mvndicraft:music.battle.greek",
            "mvndi:eastern_mediterranean_plains" to "mvndicraft:music.battle.greek",

            "mvndi:bandit_north_african" to "mvndicraft:music.battle.north_african",

            "mvndi:bandit_scandinavian" to "mvndicraft:music.battle.northern_european",
        )
    }

    override fun onEnable() {
        logger.info("Enabling MvndiSoundscapes")
        Bukkit.getPluginManager().registerEvents(this, this)
        if (Bukkit.getPluginManager().isPluginEnabled("SiegeWar")) Bukkit.getPluginManager()
            .registerEvents(SiegeWarListener(), this)
        startTasks()
    }

    override fun onDisable() {
        logger.info("Disabling MvndiSoundscapes")
    }

    private fun startTasks() {
        val ambience = AmbienceTask()
        val wind = WindTask()

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
        startedTasks.remove(uuid)
    }

    @EventHandler
    fun onPlayerFirstMove(event: PlayerMoveEvent) {
        val player = event.player
        val uuid = player.uniqueId
        val aether = player.location.world.name == "aether"

        if (startedTasks.contains(uuid)) return

        startedTasks.add(uuid)

        player.scheduler.runAtFixedRate(this, {
            if (Bukkit.getPluginManager().isPluginEnabled("SiegeWar") && SiegeWarAPI.getSiege(player).isPresent) {
                if (!BattleSession.getBattleSession().isActive && BattleSession.getBattleSession().scheduledStartTime != null && BattleSession.getBattleSession().scheduledStartTime!! <= (1000L * 72L)) {
                    playedSiegeBriefing = true
                    player.stopSound(SoundCategory.MUSIC)
                    player.playSound(player, "mvndicraft:music.siege.briefing", SoundCategory.MUSIC, 1.0f, 1.0f)
                    lastPlayed[uuid] = System.currentTimeMillis()
                    return@runAtFixedRate
                } else if (BattleSession.getBattleSession().isActive && BattleSession.getBattleSession().timeRemainingUntilBattleSessionEnds <= (1000L * 182L) && !playedSiegeEnd) {
                    playedSiegeEnd = true
                    player.stopSound(SoundCategory.MUSIC)
                    player.playSound(player, "mvndicraft:music.siege.end", SoundCategory.MUSIC, 1.0f, 1.0f)
                    lastPlayed[uuid] = System.currentTimeMillis()
                    return@runAtFixedRate
                }

                val siege = SiegeWarAPI.getSiege(player).get()
                val town = siege.town as Government
                if (!siegesPlayingAttackersAdvancing.containsKey(town.uuid) && siege.bannerControlSessions.keys.any { potentialController ->
                        TownyAPI.getInstance()
                            .getResident(potentialController.uniqueId)!!.town.nation.equals(siege.attacker)
                    }) {
                    player.stopSound(SoundCategory.MUSIC)
                    player.playSound(
                        player, "mvndicraft:music.siege.attackers_advance", SoundCategory.MUSIC, 1.0f, 1.0f
                    )
                    lastPlayed[uuid] = System.currentTimeMillis()
                    siegesPlayingAttackersAdvancing[town.uuid] = true
                    return@runAtFixedRate
                }
            }

            if (lastPlayed.containsKey(uuid) && System.currentTimeMillis() - lastPlayed[uuid]!! < if (aether) 41000 else MUSIC_DELAY) return@runAtFixedRate

            if (aether) {
                player.playSound(
                    player, "mvndicraft:soundscapes.soundtrack.spawn", SoundCategory.MUSIC, 2.0f, 1.0f
                )
                lastPlayed[uuid] = System.currentTimeMillis()
                return@runAtFixedRate
            }

            if (Bukkit.getPluginManager().isPluginEnabled("MvndiBattle") && BattleTracker.getInstance()
                    .isInBattle(player.uniqueId) && (!lastBattle.containsKey(uuid) || (System.currentTimeMillis() - lastBattle[uuid]!! >= MUSIC_DELAY))
            ) {
                player.stopSound(SoundCategory.MUSIC)
                lastBattle[uuid] = System.currentTimeMillis()
                val biomeKey = NMSBiomeUtils.getBiomeKeyString(player.location)

                for (soundscape in soundscapes.keys) if (biomeKey != null) if (biomeKey.contains(soundscape) || NMSBiomeUtils.matchTag(
                        biomeKey, soundscape
                    )
                ) {
                    player.playSound(player, soundscapes[soundscape]!!, SoundCategory.MUSIC, 2.0f, 1.0f)
                    lastPlayed[uuid] = System.currentTimeMillis()
                    return@runAtFixedRate
                }
                return@runAtFixedRate
            }

            if (player.location.y < 20 && blockCount(player.location, 8, Material.AIR) >= 64) {
                player.stopSound(SoundCategory.MUSIC)
                player.playSound(
                    player, "mvndicraft:soundscapes.soundtrack.cave", SoundCategory.MUSIC, 2.0f, 1.0f
                )
                lastPlayed[uuid] = System.currentTimeMillis()
                return@runAtFixedRate
            }
        }, null, 1L, 20L)
    }
}