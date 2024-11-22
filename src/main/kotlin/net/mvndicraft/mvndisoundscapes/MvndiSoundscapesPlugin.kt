package net.mvndicraft.mvndisoundscapes

import net.mvndicraft.mvndisoundscapes.tasks.ResetSoundscapesTask
import net.mvndicraft.mvndisoundscapes.tasks.SoundscapeTask
import org.bukkit.plugin.java.JavaPlugin

class MvndiSoundscapesPlugin : JavaPlugin() {


    override fun onEnable() {
        // Plugin startup logic
        logger.info("Enabling MvndiSoundscapes")
        MvndiSoundscapes.initialize(this)
        server.globalRegionScheduler.runAtFixedRate(this, {SoundscapeTask().run()}, 60L, 10L)
        server.globalRegionScheduler.runAtFixedRate(this, {ResetSoundscapesTask().run()}, 120L, 1L)

    }

    override fun onDisable() {
        // Plugin shutdown logic
        logger.info("Disabling MvndiSoundscapes")
    }
}