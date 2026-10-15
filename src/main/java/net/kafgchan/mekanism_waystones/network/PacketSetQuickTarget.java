package net.kafgchan.mekanism_waystones.network;

import net.kafgchan.mekanism_waystones.target.TeleportLocationSource;
import net.kafgchan.mekanism_waystones.target.TeleportTarget;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketSetQuickTarget {
    private final TeleportLocationSource source;
    private final TeleportTarget target;

    public PacketSetQuickTarget(TeleportLocationSource source, TeleportTarget target) {
        this.source = source;
        this.target = target;
    }

    public TeleportLocationSource getSource() {
        return source;
    }

    public TeleportTarget getTarget() {
        return target;
    }

    public static void encode(PacketSetQuickTarget msg, FriendlyByteBuf buf) {
        msg.source.encode(buf);
        msg.target.encode(buf);
    }

    public static PacketSetQuickTarget decode(FriendlyByteBuf buf) {
        TeleportLocationSource source = TeleportLocationSource.decode(buf);
        TeleportTarget target = TeleportTarget.decode(buf);
        return new PacketSetQuickTarget(source, target);
    }

    public static void handle(PacketSetQuickTarget msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer serverPlayer = ctx.getSender();
            if (serverPlayer != null) {
                ItemStack stack = msg.source.findItem(serverPlayer);
                if (!stack.isEmpty()) {
                    stack.getOrCreateTag().put("quick_target", msg.target.toTag());
                    serverPlayer.sendSystemMessage(
                            Component.translatable("message.mekanism_waystones.quick_target_set", msg.target.name())
                                    .withStyle(ChatFormatting.AQUA)
                    );
                }
            }
        });
        ctx.setPacketHandled(true);
    }
}
