package net.mvndicraft.mvndisoundscapes

import com.google.gson.Gson
import kotlinx.serialization.json.*
import net.kyori.adventure.key.Key
import net.mvndicraft.mvndisoundscapes.data.MvndiSound
import java.io.File

class SoundPackBootstrapper(dir: File) {
    val soundsJson: File = File(dir, "assets/mvndicraft/sounds.json")

    companion object {
        val sounds = mutableMapOf<Key, MvndiSound>()
        val keyTo
    }

    // e.g.
    /*
    "soundscapes.ambient.surface.forest.night_loop": {
        "category": "ambient",
        "sounds": [
          {
            "name": "mvndicraft:ambient/surface/forest/night_loop1",
            "volume": 1
          },
          {
            "name": "mvndicraft:ambient/surface/forest/night_loop2",
            "volume": 1
          },
          {
            "name": "mvndicraft:ambient/surface/forest/night_loop3",
            "volume": 1
          }
        ]
    },
     */

    fun loadSoundsJson() {
        val json = soundsJson.readText()
        val sounds = Json.parseToJsonElement(json).jsonObject
        sounds.forEach { (key, value) ->
            val mcSound = Json.decodeFromJsonElement<MvndiSound>(value)
            mcSound.key = Key.key("mvndicraft", key)
            mcSound.volume = value.jsonObject["volume"]!!.jsonPrimitive.float
            mcSound.weight = value.jsonObject["weight"]?.jsonPrimitive?.float ?: 1.0f
            mcSound.pitch = value.jsonObject["pitch"]?.jsonPrimitive?.float ?: 1.0f
            SoundPackBootstrapper.sounds[mcSound.key] = mcSound
        }
    }

    fun getSound(key: Key): MvndiSound? {
        var v = key.value()
        v = v.substring(v.lastIndexOf("/"))
        return sounds[key]
    }
}