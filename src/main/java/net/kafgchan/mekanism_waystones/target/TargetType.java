package net.kafgchan.mekanism_waystones.target;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

/**
 * 転送先の種類 (ALL / Mekanism / Waystones のタブ絞り込みにも使用)
 */
public enum TargetType implements StringRepresentable {
    MEKANISM("mekanism"),
    WAYSTONE("waystone");

    public static final Codec<TargetType> CODEC = StringRepresentable.fromEnum(TargetType::values);

    public static final StreamCodec<ByteBuf, TargetType> STREAM_CODEC =
            ByteBufCodecs.idMapper(i -> TargetType.values()[i], TargetType::ordinal);

    private final String name;

    TargetType(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
