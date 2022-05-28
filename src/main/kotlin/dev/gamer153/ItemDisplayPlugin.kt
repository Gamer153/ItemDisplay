package dev.gamer153

import dev.gamer153.commands.ItemDisplayCommand
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bukkit.permissions.Permission
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.nio.file.Files

val itemDisplays = mutableListOf<ItemDisplay>()

object Permissions {
    val useCommand = Permission("itemdisplay.command")
    val admin = Permission("itemdisplay.admin")
    val create = Permission("itemdisplay.create")
    val edit = Permission("itemdisplay.edit")
    val editByClick = Permission("itemdisplay.edit.by-click")
    val remove = Permission("itemdisplay.remove")
    val list = Permission("itemdisplay.list")
}

class ItemDisplayPlugin : JavaPlugin() {
    companion object {
        lateinit var itemDisplayFile: File
        lateinit var instance: ItemDisplayPlugin
        fun saveItemDisplays() {
            Files.writeString(itemDisplayFile.toPath(), Json.encodeToString(itemDisplays))
        }
        fun loadItemDisplays() {
            itemDisplays.clear()
            itemDisplays += Json.decodeFromString<List<ItemDisplay>>(Files.readString(itemDisplayFile.toPath()))
        }
    }
    override fun onEnable() {
        instance = this
        dataFolder.mkdirs()
        itemDisplayFile = File(dataFolder, "item-displays.json")
        if (!itemDisplayFile.exists()) Files.writeString(itemDisplayFile.toPath(), Json.encodeToString(listOf<ItemDisplay>()))
        loadItemDisplays()
        getCommand("itemdisplay")!!.setExecutor(ItemDisplayCommand)
        logger.info("Enabled ItemDisplayer.")
    }
    override fun onDisable() {
        saveItemDisplays()
    }
}
