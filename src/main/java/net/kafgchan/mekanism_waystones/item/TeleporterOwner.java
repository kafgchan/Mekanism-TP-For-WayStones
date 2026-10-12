package net.kafgchan.mekanism_waystones.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.UUID;

/**
 * Portable Teleporter Plus の所有者情報
 */
public record TeleporterOwner(UUID uuid, String name) {
    public static final Codec<TeleporterOwner> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("uuid").forGetter(TeleporterOwner::uuid),
            Codec.STRING.fieldOf("name").forGetter(TeleporterOwner::name)
    ).apply(instance, TeleporterOwner::new));

    public static final StreamCodec<ByteBuf, TeleporterOwner> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, TeleporterOwner::uuid,
            ByteBufCodecs.STRING_UTF8, TeleporterOwner::name,
            TeleporterOwner::new
    );

    public boolean isOwner(UUID playerUuid) {
        return this.uuid.equals(playerUuid);
    }
}
