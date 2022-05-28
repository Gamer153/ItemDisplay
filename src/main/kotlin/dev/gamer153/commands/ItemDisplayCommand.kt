package dev.gamer153.commands

import dev.gamer153.ItemDisplayPlugin
import dev.gamer153.Permissions.admin
import dev.gamer153.Permissions.create
import dev.gamer153.Permissions.edit
import dev.gamer153.Permissions.list
import dev.gamer153.Permissions.remove
import dev.gamer153.Permissions.useCommand
import dev.gamer153.itemDisplays
import dev.gamer153.tools.newItemDisplay
import dev.gamer153.tools.removeItemDisplay
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.permissions.Permission
import java.util.*

object ItemDisplayCommand : TabCompleter, CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isNotEmpty()) {
            when(args[0]) {
                "create" -> {
                    if (!(sender can create)) return sender.error("You don't have permission.")
                    if (sender !is Player) return sender.error("You have to be a player to run this.")
                    val item = sender.inventory.itemInMainHand.clone().apply { amount = 1 }
                    if (item.type == Material.AIR) return sender.error("You have to hold an item!")
                    val itemDisplay = newItemDisplay(item, sender.location.toBlockLocation().add(0.5, 0.5, 0.5))
                    itemDisplays += itemDisplay
                    ItemDisplayPlugin.saveItemDisplays()
                    sender.sendMessage("Created new item display.")
                }
                "remove" -> {
                    if (!(sender can remove)) return sender.error("You don't have permission.")
                    //if (sender !is Player) return sender.error("You have to be a player to run this.")
                    try {
                        val itemDisplay = itemDisplays.find { it.itemEntity == UUID.fromString(args.getOrNull(1)) } ?: return sender.error("Item display not found.")
                        removeItemDisplay(itemDisplay)
                        itemDisplays.remove(itemDisplay)
                        ItemDisplayPlugin.saveItemDisplays()
                    } catch (_: Throwable) {
                        return sender.error("Invalid UUID!")
                    }
                    sender.sendMessage("Removed item display.")
                }
            }
        } else sender.error("Usage: /itemdisplay <action> [args]")
        return true
    }

    override fun onTabComplete(sender: CommandSender, c: Command, s: String, args: Array<out String>): List<String> {
        if (!(sender can useCommand)) return listOf()
        return when(args.size) {
            1 -> mutableListOf("help").apply {
                if (sender can admin) this += listOf("save", "reload")
                if (sender can create) add("create")
                if (sender can edit) add("edit")
                if (sender can remove) add("remove")
                if (sender can list) add("list")
            }
            2 -> itemDisplays.map { it.itemEntity.toString() }
            else -> listOf()
        }.filter { if (args[args.size - 1].isBlank()) true else it.startsWith(args[args.size - 1]) }
    }
}

infix fun CommandSender.can(perm: Permission) = hasPermission(perm)
fun CommandSender.error(msg: String): Boolean {
    sendMessage(Component.text(msg).color(NamedTextColor.RED))
    return true
}
