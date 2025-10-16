package net.mvndicraft.mvndisoundscapes

import com.gmail.goosius.siegewar.SiegeWarAPI
import com.gmail.goosius.siegewar.events.BattleSessionStartedEvent
import com.gmail.goosius.siegewar.events.SiegeEndEvent
import com.gmail.goosius.siegewar.events.SiegeWarStartEvent
import com.palmergames.bukkit.towny.TownyAPI
import com.palmergames.bukkit.towny.`object`.Resident
import net.mvndicraft.mvndisoundscapes.MvndiSoundscapes.Companion.lastPlayed
import org.bukkit.Bukkit
import org.bukkit.SoundCategory
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import kotlin.collections.set

class SiegeWarListener : Listener {
    @EventHandler
    fun onWarDeclare(event: SiegeWarStartEvent) {
        event.targetTown.residents.forEach { resident ->
            if (resident.player == null || !resident.player?.isOnline!!)
                return@forEach
            resident.player?.stopSound(SoundCategory.MUSIC)
            resident.player?.playSound(resident.player?.location ?: return@forEach, "music.siege.war_declaration", SoundCategory.MUSIC, 1f, 1f)
            lastPlayed[resident.player!!.uniqueId] = System.currentTimeMillis()
        }
        event.townOfSiegeStarter.residents.forEach { resident ->
            if (resident.player == null || !resident.player?.isOnline!!)
                return@forEach
            resident.player?.stopSound(SoundCategory.MUSIC)
            resident.player?.playSound(resident.player?.location ?: return@forEach, "music.siege.war_declaration", SoundCategory.MUSIC, 1f, 1f)
            lastPlayed[resident.player!!.uniqueId] = System.currentTimeMillis()
        }
    }

    @EventHandler
    fun onBattleSessionStart(event: BattleSessionStartedEvent) {
        Bukkit.getOnlinePlayers().forEach { player ->
            if (!SiegeWarAPI.getSiege(player).isPresent)
                return@forEach
            val uuid = player.uniqueId
            player.stopSound(SoundCategory.MUSIC)
            player.playSound(player.location, "music.siege.start", SoundCategory.MUSIC, 1f, 1f)
            lastPlayed[uuid] = System.currentTimeMillis()
        }
        MvndiSoundscapes.playedSiegeBriefing = false
    }

    @EventHandler
    fun onSiegeEnd(event: SiegeEndEvent) {
        Bukkit.getOnlinePlayers().forEach { player ->
            player.stopSound(SoundCategory.MUSIC)
            val uuid = player.uniqueId
            val resident: Resident? = TownyAPI.getInstance().getResident(player.uniqueId)
            if (resident != null && resident.hasNation() && event.siegeWinner.equals(resident.town.nation.name)) {
                player.playSound(player.location, "music.siege.victory", SoundCategory.MUSIC, 1f, 1f)
                lastPlayed[uuid] = System.currentTimeMillis()
            } else if (resident != null && resident.hasNation() && (resident.town.nation.equals(event.siege.defender) || resident.town.equals(event.siege.defender) || resident.town.nation.equals(event.siege.attacker))) {
                player.playSound(player.location, "music.siege.defeat", SoundCategory.MUSIC, 1f, 1f)
                lastPlayed[uuid] = System.currentTimeMillis()
            }
        }
        MvndiSoundscapes.playedSiegeEnd = false
    }
}