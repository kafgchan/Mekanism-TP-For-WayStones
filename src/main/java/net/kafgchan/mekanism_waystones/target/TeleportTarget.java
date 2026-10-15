package net.kafgchan.mekanism_waystones.target;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * 統合転送先データモデル (ネットワーク同期・永続化対応)
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

    public static final Codec<TeleportTarget> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TargetType.CODEC.fieldOf("type").forGetter(TeleportTarget::type),
            Codec.STRING.fieldOf("id").forGetter(TeleportTarget::id),
            Codec.STRING.fieldOf("name").forGetter(TeleportTarget::name),
            ResourceKey.codec(Registries.DIMENSION).fieldOf("dimension").forGetter(TeleportTarget::dimension),
            BlockPos.CODEC.fieldOf("pos").forGetter(TeleportTarget::pos),
            Codec.INT.fieldOf("color").forGetter(TeleportTarget::color),
            AccessScope.CODEC.optionalFieldOf("scope", AccessScope.PUBLIC).forGetter(TeleportTarget::scope)
    ).apply(instance, TeleportTarget::new));

    public static final StreamCodec<ByteBuf, TeleportTarget> STREAM_CODEC = StreamCodec.of(
            (buf, target) -> {
                TargetType.STREAM_CODEC.encode(buf, target.type());
                ByteBufCodecs.STRING_UTF8.encode(buf, target.id());
                ByteBufCodecs.STRING_UTF8.encode(buf, target.name());
                ResourceKey.streamCodec(Registries.DIMENSION).encode(buf, target.dimension());
                BlockPos.STREAM_CODEC.encode(buf, target.pos());
                ByteBufCodecs.VAR_INT.encode(buf, target.color());
                AccessScope.STREAM_CODEC.encode(buf, target.scope() != null ? target.scope() : AccessScope.PUBLIC);
            },
            buf -> new TeleportTarget(
                    TargetType.STREAM_CODEC.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ResourceKey.streamCodec(Registries.DIMENSION).decode(buf),
                    BlockPos.STREAM_CODEC.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    AccessScope.STREAM_CODEC.decode(buf)
            )
    );
}
