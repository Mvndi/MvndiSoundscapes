package net.mvndicraft.mvndisoundscapes

import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Logger

object MvndiSoundscapes {

    internal var plugin: Plugin? = null
    internal var logger: Logger? = null
    var WIND_KEY: NamespacedKey? = null
    var AETHER_KEY: NamespacedKey? = null
    var playerSounds: ConcurrentHashMap<Player, Long> = ConcurrentHashMap()
    var soundscapes = mapOf("nile" to "mvndicraft:soundscapes.soundtrack.egypt", "arabian" to "mvndicraft:soundscapes.soundtrack.egypt", "greece" to "mvndicraft:soundscapes.soundtrack.greece", "italy" to "mvndicraft:soundscapes.soundtrack.italy", "germany" to "mvndicraft:soundscapes.soundtrack.germany")

    internal fun initialize(plugin: Plugin) {
        MvndiSoundscapes.plugin = plugin
        logger = plugin.logger
        WIND_KEY = NamespacedKey(MvndiSoundscapes.plugin!!, "wind")
        AETHER_KEY = NamespacedKey(MvndiSoundscapes.plugin!!, "aether")
    }

}
