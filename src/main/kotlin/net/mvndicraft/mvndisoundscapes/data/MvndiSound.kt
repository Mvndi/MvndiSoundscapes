package net.mvndicraft.mvndisoundscapes.data

import kotlinx.serialization.Serializable
import net.kyori.adventure.key.Key
import net.mvndicraft.lib.co.aikar.commands.annotation.Optional
import org.bukkit.SoundCategory

@Serializable
data class MvndiSound(
    var key: Key,
    val category: SoundCategory,
    @Optional var weight: Float = 1.0f,
    @Optional var pitch: Float = 1.0f,
    var volume: Float,
    var duration: Long
)