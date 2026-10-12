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
        int color
) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(type);
        buf.writeUtf(id);
        buf.writeUtf(name);
        buf.writeResourceLocation(dimension.location());
        buf.writeBlockPos(pos);
        buf.writeVarInt(color);
    }

    public static TeleportTarget decode(FriendlyByteBuf buf) {
        TargetType type = buf.readEnum(TargetType.class);
        String id = buf.readUtf();
        String name = buf.readUtf();
        ResourceLocation dimLoc = buf.readResourceLocation();
        ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, dimLoc);
        BlockPos pos = buf.readBlockPos();
        int color = buf.readVarInt();
        return new TeleportTarget(type, id, name, dimension, pos, color);
    }
}
