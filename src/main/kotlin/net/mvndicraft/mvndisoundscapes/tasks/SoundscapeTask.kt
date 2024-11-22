package net.mvndicraft.mvndisoundscapes.tasks

import net.mvndicraft.mvndiseasons.biomes.NMSBiomeUtils
import net.mvndicraft.mvndisoundscapes.MvndiSoundscapes
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.SoundCategory
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType

class SoundscapeTask : Runnable {
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

    override fun run() {
        Bukkit.getOnlinePlayers().parallelStream().forEach { player: Player? ->
            player!!.scheduler.run(MvndiSoundscapes.plugin!!, {
                val pdc = player.persistentDataContainer
                if (player.location.world.name == "aether") {
                    if (!pdc.has(MvndiSoundscapes.AETHER_KEY!!) || (pdc.has(MvndiSoundscapes.AETHER_KEY!!) && pdc.get(
                            MvndiSoundscapes.AETHER_KEY!!, PersistentDataType.INTEGER
                        ) == 0)
                    ) {
                        player.playSound(
                            player, "mvndicraft:soundscapes.soundtrack.spawn", SoundCategory.MUSIC, 1.2f, 1.0f
                        )
                        pdc.set(MvndiSoundscapes.AETHER_KEY!!, PersistentDataType.INTEGER, 1)
                    }
                    return@run
                }

                val to = player.location
                if (to.y >= 128 && airCount(to, 8) >= 128) {
                    if (!pdc.has(MvndiSoundscapes.WIND_KEY!!) || (pdc.has(MvndiSoundscapes.WIND_KEY!!) && pdc.get(
                            MvndiSoundscapes.WIND_KEY!!, PersistentDataType.INTEGER
                        ) == 0)
                    ) {
                        player.playSound(
                            player,
                            "mvndicraft:soundscapes.ambient.surface.wind.loop",
                            SoundCategory.AMBIENT,
                            1.2f,
                            1.0f
                        )
                        pdc.set(MvndiSoundscapes.WIND_KEY!!, PersistentDataType.INTEGER, 1)
                    }
                }

                if (Bukkit.getPluginManager().isPluginEnabled("MvndiSeasons")) {
                    val mvndibiomeName = NMSBiomeUtils.getBiomeKeyString(player.location)

                    MvndiSoundscapes.soundscapes.keys.parallelStream().forEach { soundscape ->

                        if (!MvndiSoundscapes.playerSounds.containsKey(player)) MvndiSoundscapes.playerSounds[player] =
                            System.currentTimeMillis()
                        if (System.currentTimeMillis() - MvndiSoundscapes.playerSounds[player]!! < 420000) return@forEach

                        if (mvndibiomeName.contains(soundscape)) {
                            player.playSound(
                                player, MvndiSoundscapes.soundscapes[soundscape]!!, SoundCategory.MUSIC, 0.5f, 1.0f
                            )
                            MvndiSoundscapes.playerSounds[player] = System.currentTimeMillis()
                            return@forEach
                        }
                    }
                }

                val biomeName = to.block.biome.name

                if (!MvndiSoundscapes.playerSounds.containsKey(player)) MvndiSoundscapes.playerSounds[player] =
                    System.currentTimeMillis()

                if (System.currentTimeMillis() - MvndiSoundscapes.playerSounds[player]!! < 420000) return@run

                val time = player.world.time
                val day = (time in 12000..22999)
                val lower = (time in 12000..12500) || (time in 13000..22999)

                if (biomeName.contains("PLAINS")) {
                    player.playSound(
                        player, "mvndicraft:soundscapes.soundtrack.plains", SoundCategory.MUSIC, 1.2f, 1.0f
                    )
                    player.playSound(
                        player, "mvndicraft:soundscapes.ambient.surface.plains", SoundCategory.AMBIENT, 0.5f, 1.0f
                    )
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
                } else if (biomeName.contains("SNOW")) {
                    player.playSound(
                        player, "mvndicraft:soundscapes.soundtrack.snowy", SoundCategory.MUSIC, 1.2f, 1.0f
                    )
                    if (player.world.hasStorm()) player.playSound(
                        player,
                        "mvndicraft:soundscapes.ambient.surface.weather.snowy",
                        SoundCategory.AMBIENT,
                        0.5f,
                        1.0f
                    )
                } else if (biomeName.contains("BEACH")) {
                    player.playSound(
                        player,
                        "mvndicraft:soundscapes.ambient.surface.beach.loop",
                        SoundCategory.AMBIENT,
                        if (lower) 0.1f else 0.15f,
                        1.0f
                    )
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
                            player,
                            "mvndicraft:soundscapes.ambient.surface.swamp.night_loop",
                            SoundCategory.AMBIENT,
                            0.5f,
                            1.0f
                        )
                    }
                } else if (biomeName.contains("RIVER")) {
                    player.playSound(
                        player, "mvndicraft:soundscapes.ambient.surface.river.loop", SoundCategory.AMBIENT, 60f, 1.0f
                    )
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
                }
                MvndiSoundscapes.playerSounds[player] = System.currentTimeMillis()
            }, { MvndiSoundscapes.logger!!.severe("Failed to run SoundscapeTask") })
        }
    }
}