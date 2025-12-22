package net.mvndicraft.mvndisoundscapes

import com.gmail.goosius.siegewar.SiegeWarAPI
import com.gmail.goosius.siegewar.objects.BattleSession
import com.palmergames.bukkit.towny.TownyAPI
import com.palmergames.bukkit.towny.`object`.Government
import fr.formiko.mc.biomeutils.NMSBiomeUtils
import net.jodah.expiringmap.ExpiringMap
import net.mvndicraft.mvndibattle.BattleTracker
import net.mvndicraft.mvndicore.MvndiCore
import net.mvndicraft.mvndicore.commands.subcommands.ReloadCommand
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.SoundCategory
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.min

data class Region(val minX: Int, val maxX: Int, val minZ: Int, val maxZ: Int, val sound: String)

class MvndiSoundscapes : JavaPlugin(), Listener {
    companion object {
        var musicDelay: Long = 420000L
        var ambienceDelay: Long = 8000L
        var aetherDelay: Long = 204000L
        var aetherWorldName: String = "aether"
        var aetherSound: String = "mvndicraft:music.main"
        var caveYThreshold: Int = 20
        var caveAirThreshold: Int = 64
        var caveRadius: Int = 8
        var caveSound: String = "mvndicraft:music.cave"
        var siegeBriefingSeconds: Long = 72L
        var siegeEndSeconds: Long = 182L
        var siegeAttackersAdvanceDuration: Long = 46L
        var siegeBriefingSound: String = "mvndicraft:music.siege.briefing"
        var siegeEndSound: String = "mvndicraft:music.siege.end"
        var siegeAttackersAdvanceSound: String = "mvndicraft:music.siege.attackers_advance"
        var soundscapes: Map<String, String> = emptyMap()

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
            ExpiringMap.builder().variableExpiration().expiration(siegeAttackersAdvanceDuration, TimeUnit.SECONDS)
                .build<UUID, Boolean>()
        var battleRegions: List<Region> = emptyList()
        var regions: List<Region> = emptyList()
    }

    override fun onEnable() {
        logger.info("Enabling MvndiSoundscapes")
        saveDefaultConfig()
        loadConfig()
        val pm = Bukkit.getPluginManager()
        pm.registerEvents(this, this)
        if (pm.isPluginEnabled("SiegeWar")) pm.registerEvents(SiegeWarListener(), this)
        startTasks()
        if (pm.isPluginEnabled("MvndiCore")) {
            MvndiCore.getInstance().registerModule("MvndiSoundscapes")
            MvndiCore.getInstance().registerSubCommand(ReloadCommand("soundscapes", this))
        }
    }

    override fun onDisable() {
        logger.info("Disabling MvndiSoundscapes")
    }

    override fun reloadConfig() {
        super.reloadConfig()
        loadConfig()
    }

    private fun loadConfig() {
        val config = getConfig()
        musicDelay = config.getLong("music_delay", 420000L)
        ambienceDelay = config.getLong("ambience_delay", 8000L)
        aetherDelay = config.getLong("aether_delay", 204000L)
        aetherWorldName = config.getString("aether_world", "aether")!!
        aetherSound = config.getString("aether_sound", "mvndicraft:music.main")!!
        caveYThreshold = config.getInt("cave.y_threshold", 20)
        caveAirThreshold = config.getInt("cave.air_threshold", 64)
        caveRadius = config.getInt("cave.radius", 8)
        caveSound = config.getString("cave.sound", "mvndicraft:music.cave")!!
        siegeBriefingSeconds = config.getLong("siege.briefing_seconds", 72L)
        siegeEndSeconds = config.getLong("siege.end_seconds", 182L)
        siegeAttackersAdvanceDuration = config.getLong("siege.attackers_advance_duration", 46L)
        siegeBriefingSound = config.getString("siege.briefing_sound", "mvndicraft:music.siege.briefing")!!
        siegeEndSound = config.getString("siege.end_sound", "mvndicraft:music.siege.end")!!
        siegeAttackersAdvanceSound =
            config.getString("siege.attackers_advance_sound", "mvndicraft:music.siege.attackers_advance")!!

        val soundscapesMap = mutableMapOf<String, String>()
        val biomeSec = config.getConfigurationSection("biome_tags")
        if (biomeSec != null) {
            for (key in biomeSec.getKeys(false)) {
                soundscapesMap[key] = biomeSec.getString(key)!!
            }
        }
        soundscapes = soundscapesMap

        val battleRegionList = mutableListOf<Region>()
        val battleSec = config.getConfigurationSection("battle_regions")
        if (battleSec != null) {
            for (key in battleSec.getKeys(false)) {
                val section = battleSec.getConfigurationSection(key)!!
                val enabled = section.getBoolean("enabled")
                if (enabled) {
                    val sound = section.getString("sound")!!
                    val x = section.getInt("x")
                    val z = section.getInt("z")
                    val x1 = section.getInt("x_1")
                    val z1 = section.getInt("z_1")
                    val minX = min(x, x1)
                    val maxX = max(x, x1)
                    val minZ = min(z, z1)
                    val maxZ = max(z, z1)
                    battleRegionList.add(Region(minX, maxX, minZ, maxZ, sound))
                }
            }
        }
        battleRegions = battleRegionList

        val regionList = mutableListOf<Region>()
        val regionsSec = config.getConfigurationSection("regions")
        if (regionsSec != null) {
            for (key in regionsSec.getKeys(false)) {
                val section = regionsSec.getConfigurationSection(key)!!
                val enabled = section.getBoolean("enabled")
                if (enabled) {
                    val sound = section.getString("sound")!!
                    val x = section.getInt("x")
                    val z = section.getInt("z")
                    val x1 = section.getInt("x_1")
                    val z1 = section.getInt("z_1")
                    val minX = min(x, x1)
                    val maxX = max(x, x1)
                    val minZ = min(z, z1)
                    val maxZ = max(z, z1)
                    regionList.add(Region(minX, maxX, minZ, maxZ, sound))
                }
            }
        }
        regions = regionList

        siegesPlayingAttackersAdvancing.setExpiration(siegeAttackersAdvanceDuration, TimeUnit.SECONDS)
    }

    private fun startTasks() {
        val ambience = AmbienceTask()
        val wind = WindTask()

        Bukkit.getGlobalRegionScheduler().runAtFixedRate(this, {
            Bukkit.getOnlinePlayers().forEach { player ->
                player.scheduler.run(this, {
                    ambience.run(player)
                    if (player.location.world.name != aetherWorldName) wind.run(player)
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
    fun onPlayerFirstMove(event: PlayerJoinEvent) {
        val player = event.player
        val uuid = player.uniqueId
        val aether = player.location.world.name == aetherWorldName

        if (startedTasks.contains(uuid)) return

        startedTasks.add(uuid)

        player.scheduler.runAtFixedRate(this, {
            if (Bukkit.getPluginManager().isPluginEnabled("SiegeWar") && SiegeWarAPI.getSiege(player).isPresent) {
                if (!BattleSession.getBattleSession().isActive && BattleSession.getBattleSession().scheduledStartTime != null && BattleSession.getBattleSession().scheduledStartTime!! <= (1000L * siegeBriefingSeconds)) {
                    playedSiegeBriefing = true
                    player.stopSound(SoundCategory.MUSIC)
                    player.playSound(player, siegeBriefingSound, SoundCategory.MUSIC, 1.0f, 1.0f)
                    lastPlayed[uuid] = System.currentTimeMillis()
                    return@runAtFixedRate
                } else if (BattleSession.getBattleSession().isActive && BattleSession.getBattleSession().timeRemainingUntilBattleSessionEnds <= (1000L * siegeEndSeconds) && !playedSiegeEnd) {
                    playedSiegeEnd = true
                    player.stopSound(SoundCategory.MUSIC)
                    player.playSound(player, siegeEndSound, SoundCategory.MUSIC, 1.0f, 1.0f)
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
                        player, siegeAttackersAdvanceSound, SoundCategory.MUSIC, 1.0f, 1.0f
                    )
                    lastPlayed[uuid] = System.currentTimeMillis()
                    siegesPlayingAttackersAdvancing[town.uuid] = true
                    return@runAtFixedRate
                }
            }

            if (Bukkit.getPluginManager().isPluginEnabled("MvndiBattle") && BattleTracker.getInstance()
                    .isInBattle(player.uniqueId) && (!lastBattle.containsKey(uuid) || (System.currentTimeMillis() - lastBattle[uuid]!! >= musicDelay))
            ) {
                player.stopSound(SoundCategory.MUSIC)
                lastBattle[uuid] = System.currentTimeMillis()
                val biomeKey = NMSBiomeUtils.getBiomeKeyString(player.location)
                var played = false
                if (biomeKey != null) {
                    for ((key, sound) in soundscapes) {
                        if (biomeKey.contains(key) || NMSBiomeUtils.matchTag(biomeKey, key)) {
                            player.playSound(player, sound, SoundCategory.MUSIC, 1.0f, 1.0f)
                            lastPlayed[uuid] = System.currentTimeMillis()
                            played = true
                            break
                        }
                    }
                }
                if (!played) {
                    for (region in battleRegions) {
                        if (player.location.blockX in region.minX..region.maxX && player.location.blockZ in region.minZ..region.maxZ) {
                            player.playSound(player, region.sound, SoundCategory.MUSIC, 1.0f, 1.0f)
                            lastPlayed[uuid] = System.currentTimeMillis()
                            played = true
                            break
                        }
                    }
                }
                return@runAtFixedRate
            }

            if (lastPlayed.containsKey(uuid) && System.currentTimeMillis() - lastPlayed[uuid]!! < if (aether) aetherDelay else musicDelay) return@runAtFixedRate

            if (aether) {
                player.playSound(
                    player, aetherSound, SoundCategory.MUSIC, 1.0f, 1.0f
                )
                lastPlayed[uuid] = System.currentTimeMillis()
                return@runAtFixedRate
            }

            if (player.location.y < caveYThreshold && blockCount(
                    player.location, caveRadius, Material.AIR
                ) >= caveAirThreshold
            ) {
                player.stopSound(SoundCategory.MUSIC)
                player.playSound(
                    player, caveSound, SoundCategory.MUSIC, 1.0f, 1.0f
                )
                lastPlayed[uuid] = System.currentTimeMillis()
                return@runAtFixedRate
            }

            for (region in regions) {
                if (player.location.blockX in region.minX..region.maxX && player.location.blockZ in region.minZ..region.maxZ) {
                    player.stopSound(SoundCategory.MUSIC)
                    player.playSound(player, region.sound, SoundCategory.MUSIC, 1.0f, 1.0f)
                    lastPlayed[uuid] = System.currentTimeMillis()
                    break
                }
            }
        }, null, 1L, 20L)
    }
}