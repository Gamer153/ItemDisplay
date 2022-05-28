package dev.gamer153

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.inventory.ItemStack
import java.util.*

@Serializable
data class ItemDisplay(
    @Serializable(with = UUIDSerializer::class) val itemEntity: UUID,
    @Serializable(with = UUIDSerializer::class) val blockEntity: UUID,
    @Serializable(with = ItemStackSerializer::class) val item: ItemStack,
    val location: SLocation
)

class UUIDSerializer: KSerializer<UUID> {
    override val descriptor = PrimitiveSerialDescriptor("uuid", PrimitiveKind.STRING)
    override fun deserialize(decoder: Decoder): UUID = UUID.fromString(decoder.decodeString())
    override fun serialize(encoder: Encoder, value: UUID) = encoder.encodeString(value.toString())
}

class ItemStackSerializer: KSerializer<ItemStack> {
    override val descriptor = ByteArraySerializer().descriptor
    override fun deserialize(decoder: Decoder)
        = ItemStack.deserializeBytes(decoder.decodeSerializableValue(ByteArraySerializer()))
    override fun serialize(encoder: Encoder, value: ItemStack)
        = encoder.encodeSerializableValue(ByteArraySerializer(), value.serializeAsBytes())
}

@Serializable
data class SLocation(val x: Double, val y: Double, val z: Double, val world: String) {
    constructor(location: Location) : this(location.x, location.y, location.z, location.world.name)
    fun location() = Location(Bukkit.getWorld(world), x, y, z)
}
