package net.kafgchan.mekanism_waystones.item;

import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

/**
 * Portable Teleporter Plus の所有者情報 (UUID & Name)
 */
public record TeleporterOwner(UUID uuid, String name) {

    public boolean isOwner(UUID playerUuid) {
        return this.uuid.equals(playerUuid);
    }

    public CompoundTag saveToNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("uuid", this.uuid);
        tag.putString("name", this.name);
        return tag;
    }

    public static TeleporterOwner fromNbt(CompoundTag tag) {
        if (tag == null || !tag.hasUUID("uuid") || !tag.contains("name")) {
            return null;
        }
        return new TeleporterOwner(tag.getUUID("uuid"), tag.getString("name"));
    }
}
