package net.kafgchan.mekanism_waystones.target;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * 転送先の種類 (ALL / Mekanism / Waystones のタブ絞り込みにも使用)
 */
public enum TargetType {
    MEKANISM,
    WAYSTONE;

    public static final StreamCodec<ByteBuf, TargetType> STREAM_CODEC =
            ByteBufCodecs.idMapper(i -> TargetType.values()[i], TargetType::ordinal);
}
