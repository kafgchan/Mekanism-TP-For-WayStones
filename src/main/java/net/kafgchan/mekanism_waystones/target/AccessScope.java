package net.kafgchan.mekanism_waystones.target;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;

public enum AccessScope implements StringRepresentable {
    PUBLIC("gui.mekanism_waystones.scope.public", "gui.mekanism_waystones.scope.public.badge", 0xFF00E5FF),
    PRIVATE("gui.mekanism_waystones.scope.private", "gui.mekanism_waystones.scope.private.badge", 0xFFA0AAB5),
    TRUSTED("gui.mekanism_waystones.scope.trusted", "gui.mekanism_waystones.scope.trusted.badge", 0xFF76FF03),
    PERSONAL("gui.mekanism_waystones.scope.personal", "gui.mekanism_waystones.scope.personal.badge", 0xFFAA55FF),
    SHARED("gui.mekanism_waystones.scope.shared", "gui.mekanism_waystones.scope.shared.badge", 0xFFFFB300);

    public static final Codec<AccessScope> CODEC = StringRepresentable.fromEnum(AccessScope::values);
    public static final StreamCodec<ByteBuf, AccessScope> STREAM_CODEC = ByteBufCodecs.idMapper(i -> values()[i], AccessScope::ordinal);

    private final String translationKey;
    private final String badgeKey;
    private final int color;

    AccessScope(String translationKey, String badgeKey, int color) {
        this.translationKey = translationKey;
        this.badgeKey = badgeKey;
        this.color = color;
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public Component getDisplayName() {
        return Component.translatable(translationKey);
    }

    public Component getBadgeComponent() {
        return Component.translatable(badgeKey);
    }

    public String getBadgeText() {
        return Component.translatable(badgeKey).getString();
    }

    public int getColor() {
        return color;
    }
}
