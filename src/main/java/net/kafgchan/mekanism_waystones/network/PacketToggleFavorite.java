package net.kafgchan.mekanism_waystones.network;

import net.kafgchan.mekanism_waystones.target.PlayerFavoritesService;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketToggleFavorite {
    private final String targetKey;

    public PacketToggleFavorite(String targetKey) {
        this.targetKey = targetKey;
    }

    public String getTargetKey() {
        return targetKey;
    }

    public static void encode(PacketToggleFavorite msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.targetKey);
    }

    public static PacketToggleFavorite decode(FriendlyByteBuf buf) {
        return new PacketToggleFavorite(buf.readUtf());
    }

    public static void handle(PacketToggleFavorite msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer serverPlayer = ctx.getSender();
            if (serverPlayer != null) {
                PlayerFavoritesService.toggleFavorite(serverPlayer, msg.targetKey);
            }
        });
        ctx.setPacketHandled(true);
    }
}
