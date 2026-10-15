package net.kafgchan.mekanism_waystones.target;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/**
 * 統合転送先データモデル (ネットワーク同期対応)
 */
public record TeleportTarget(
        TargetType type,
        String id,
        String name,
        ResourceKey<Level> dimension,
        BlockPos pos,
        int color,
        AccessScope scope
) {
    public TeleportTarget(TargetType type, String id, String name, ResourceKey<Level> dimension, BlockPos pos, int color) {
        this(type, id, name, dimension, pos, color, type == TargetType.MEKANISM ? AccessScope.PUBLIC : AccessScope.PERSONAL);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(type);
        buf.writeUtf(id);
        buf.writeUtf(name);
        buf.writeResourceLocation(dimension.location());
        buf.writeBlockPos(pos);
        buf.writeVarInt(color);
        buf.writeEnum(scope != null ? scope : (type == TargetType.MEKANISM ? AccessScope.PUBLIC : AccessScope.PERSONAL));
    }

    public static TeleportTarget decode(FriendlyByteBuf buf) {
        TargetType type = buf.readEnum(TargetType.class);
        String id = buf.readUtf();
        String name = buf.readUtf();
        ResourceLocation dimLoc = buf.readResourceLocation();
        ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, dimLoc);
        BlockPos pos = buf.readBlockPos();
        int color = buf.readVarInt();
        AccessScope scope = buf.readEnum(AccessScope.class);
        return new TeleportTarget(type, id, name, dimension, pos, color, scope);
    }

    public net.minecraft.nbt.CompoundTag toTag() {
        net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
        tag.putString("type", type.name());
        tag.putString("id", id);
        tag.putString("name", name);
        tag.putString("dimension", dimension.location().toString());
        tag.putLong("pos", pos.asLong());
        tag.putInt("color", color);
        tag.putString("scope", (scope != null ? scope : (type == TargetType.MEKANISM ? AccessScope.PUBLIC : AccessScope.PERSONAL)).name());
        return tag;
    }

    public static TeleportTarget fromTag(net.minecraft.nbt.CompoundTag tag) {
        if (tag == null || !tag.contains("type")) return null;
        try {
            TargetType type = TargetType.valueOf(tag.getString("type"));
            String id = tag.getString("id");
            String name = tag.getString("name");
            ResourceLocation dimLoc = ResourceLocation.tryParse(tag.getString("dimension"));
            if (dimLoc == null) return null;
            ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, dimLoc);
            BlockPos pos = BlockPos.of(tag.getLong("pos"));
            int color = tag.getInt("color");
            AccessScope scope = tag.contains("scope")
                    ? AccessScope.valueOf(tag.getString("scope"))
                    : (type == TargetType.MEKANISM ? AccessScope.PUBLIC : AccessScope.PERSONAL);
            return new TeleportTarget(type, id, name, dimension, pos, color, scope);
        } catch (Exception e) {
            return null;
        }
    }
}
