package net.kafgchan.mekanism_waystones.target;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
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
    public static final StreamCodec<ByteBuf, TeleportTarget> STREAM_CODEC = StreamCodec.composite(
            TargetType.STREAM_CODEC, TeleportTarget::type,
            ByteBufCodecs.STRING_UTF8, TeleportTarget::id,
            ByteBufCodecs.STRING_UTF8, TeleportTarget::name,
            ResourceKey.streamCodec(Registries.DIMENSION), TeleportTarget::dimension,
            BlockPos.STREAM_CODEC, TeleportTarget::pos,
            ByteBufCodecs.VAR_INT, TeleportTarget::color,
            TeleportTarget::new
    );
}
