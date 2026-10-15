package net.kafgchan.mekanism_waystones.network;

import io.netty.buffer.ByteBuf;
import net.kafgchan.mekanism_waystones.MekanismTPforWaystones;
import net.kafgchan.mekanism_waystones.target.PlayerFavoritesService;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketToggleFavorite(String targetKey) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PacketToggleFavorite> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MekanismTPforWaystones.MODID, "toggle_favorite"));

    public static final StreamCodec<ByteBuf, PacketToggleFavorite> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            PacketToggleFavorite::targetKey,
            PacketToggleFavorite::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketToggleFavorite packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                PlayerFavoritesService.toggleFavorite(serverPlayer, packet.targetKey());
            }
        });
    }
}
