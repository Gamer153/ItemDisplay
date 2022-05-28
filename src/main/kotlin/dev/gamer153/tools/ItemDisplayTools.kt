package dev.gamer153.tools

import dev.gamer153.ItemDisplay
import dev.gamer153.ItemDisplayPlugin
import dev.gamer153.SLocation
import dev.gamer153.itemDisplays
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Item
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.util.Vector


fun newItemDisplay(item: ItemStack, location: Location): ItemDisplay {
    val itemEntity = location.world.spawnEntity(location, EntityType.DROPPED_ITEM) as Item
    itemEntity.setWillAge(false)
    itemEntity.setCanMobPickup(false)
    itemEntity.setCanPlayerPickup(false)
    itemEntity.isCustomNameVisible = true
    itemEntity.customName(item.displayName())
    itemEntity.itemStack = item
    itemEntity.setGravity(false)
    itemEntity.isGlowing = true
    itemEntity.isUnlimitedLifetime = true
    val glassEntity = location.world.spawnFallingBlock(location, Material.GLASS.createBlockData())
    glassEntity.setGravity(false)
    glassEntity.setHurtEntities(false)
    glassEntity.shouldAutoExpire(false)
    glassEntity.dropItem = false

    glassEntity.velocity = Vector()
    itemEntity.velocity = Vector()
    itemEntity.teleport(location.clone().add(.0, .5, .0))

    itemEntity.setMetadata("linked_glass", FixedMetadataValue(ItemDisplayPlugin.instance, glassEntity.uniqueId.toString()))
    glassEntity.setMetadata("linked_item", FixedMetadataValue(ItemDisplayPlugin.instance, itemEntity.uniqueId.toString()))
    return ItemDisplay(itemEntity.uniqueId, glassEntity.uniqueId, item, SLocation(location))
}

fun findItemDisplays(location: Location, boxRadius: Double)
    = location.world.getNearbyEntities(location, boxRadius, boxRadius, boxRadius) { it.type == EntityType.DROPPED_ITEM && it.hasMetadata("linked_glass") }
        .mapNotNull { ent -> itemDisplays.find { it.itemEntity == ent.uniqueId } }

fun removeItemDisplay(itemDisplay: ItemDisplay) {
    with(itemDisplay.location.location().world) {
        getEntity(itemDisplay.itemEntity)?.remove()
        getEntity(itemDisplay.blockEntity)?.remove()
    }
}
