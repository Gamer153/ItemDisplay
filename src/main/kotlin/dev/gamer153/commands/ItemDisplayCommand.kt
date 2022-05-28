package dev.gamer153.commands

import dev.gamer153.ItemDisplayPlugin
import dev.gamer153.Permissions.admin
import dev.gamer153.Permissions.create
import dev.gamer153.Permissions.list
import dev.gamer153.Permissions.remove
import dev.gamer153.Permissions.useCommand
import dev.gamer153.itemDisplays
import dev.gamer153.tools.newItemDisplay
import dev.gamer153.tools.removeItemDisplay
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.EntityType
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
                    val itemDisplay = newItemDisplay(item, sender.location.toBlockLocation().add(0.5, .0, 0.5))
                    itemDisplays += itemDisplay
                    ItemDisplayPlugin.saveItemDisplays()
                    sender.sendMessage("Created new item display.".comp(NamedTextColor.GREEN))
                }
                "remove" -> {
                    if (!(sender can remove)) return sender.error("You don't have permission.")
                    if (args.size == 1) return sender.error(if (sender is Player) "Usage: /itemdisplay remove <look|UUID>" else "Usage: /itemdisplay remove <UUID>")
                    if (sender !is Player && args[1] == "look") return sender.error("You have to be a player to run this.")
                    try {
                        val itemDisplay = if (args[1] == "look" && sender is Player) {
                            val target = sender.getTargetEntity(4) ?: return sender.error("You're not looking at any item display.")
                            if ((target.type == EntityType.FALLING_BLOCK && target.hasMetadata("linked_item"))
                                || (target.type == EntityType.DROPPED_ITEM && target.hasMetadata("linked_glass"))) {
                                itemDisplays.find { if (target.type == EntityType.FALLING_BLOCK) it.blockEntity == target.uniqueId else it.itemEntity == target.uniqueId } ?: return sender.error("No item display found.")
                            } else return sender.error("You're not looking at an item display.")
                        } else
                            itemDisplays.find { it.itemEntity == UUID.fromString(args.getOrNull(1)) } ?: return sender.error("Item display not found.")
                        removeItemDisplay(itemDisplay)
                        itemDisplays.remove(itemDisplay)
                        ItemDisplayPlugin.saveItemDisplays()
                    } catch (_: Throwable) {
                        return sender.error("Invalid UUID!")
                    }
                    sender.sendMessage("Removed item display.".comp(NamedTextColor.DARK_GREEN))
                }
                "list" -> {
                    if (!(sender can list)) return sender.error("You don't have permission.")
                    sender.sendMessage("Item Displays:".comp(NamedTextColor.AQUA).decorate(TextDecoration.UNDERLINED).run {
                        Component.join(JoinConfiguration.newlines(), this, *itemDisplays.map { Component.join(
                            JoinConfiguration.noSeparators(),
                            "  ".comp(),
                            "[x] ".comp().color(NamedTextColor.RED)
                                .hoverEvent(HoverEvent.showText("Suggest delete command".comp()))
                                .clickEvent(ClickEvent.suggestCommand("/itemdisplay remove " + it.itemEntity)),
                            it.item.displayName(),
                            " at ".comp(NamedTextColor.AQUA),
                            it.location.toString().comp(NamedTextColor.DARK_AQUA)
                                .hoverEvent(HoverEvent.showText("Click to teleport".comp()))
                                .clickEvent(ClickEvent.runCommand("/itemdisplay run-cmd-teleport-153 ${it.itemEntity}"))
                        )}.toTypedArray().let { if (it.isEmpty()) arrayOf("  No item displays".comp(NamedTextColor.DARK_AQUA).decorate(TextDecoration.ITALIC)) else it })
                    })
                }
                "run-cmd-teleport-153" -> {
                    if (!(sender can list) || args.size < 2 || sender !is Player) return true
                    try {
                        itemDisplays.find { it.itemEntity == UUID.fromString(args[1]) }
                            ?.let { sender.teleport(it.location.location()) }
                    } catch (_: Throwable) {}
                }
                "save" -> {
                    ItemDisplayPlugin.saveItemDisplays()
                    sender.sendMessage("Saved item-displays.json.".comp(NamedTextColor.GREEN))
                }
                "reload" -> {
                    ItemDisplayPlugin.reloadItemDisplays()
                    sender.sendMessage("Reloaded item displays.".comp(NamedTextColor.GREEN))
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
//                if (sender can edit) add("edit") TODO: Add edit mode when right-clicking
                if (sender can remove) add("remove")
                if (sender can list) add("list")
            }
            2 -> itemDisplays.map { it.itemEntity.toString() }.toMutableList().apply { add("look") }
            else -> listOf()
        }.filter { if (args[args.size - 1].isBlank()) true else it.startsWith(args[args.size - 1]) }
    }
}

infix fun CommandSender.can(perm: Permission) = hasPermission(perm)
fun CommandSender.error(msg: String): Boolean {
    sendMessage(Component.text(msg).color(NamedTextColor.RED))
    return true
}
fun String.comp(color: TextColor? = null) = Component.text(this).color(color)
