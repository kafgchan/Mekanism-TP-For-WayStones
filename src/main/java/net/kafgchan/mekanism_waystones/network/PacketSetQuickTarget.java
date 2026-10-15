package net.kafgchan.mekanism_waystones.network;

import io.netty.buffer.ByteBuf;
import net.kafgchan.mekanism_waystones.MekanismTPforWaystones;
import net.kafgchan.mekanism_waystones.registration.ModDataComponents;
import net.kafgchan.mekanism_waystones.target.TeleportLocationSource;
import net.kafgchan.mekanism_waystones.target.TeleportTarget;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketSetQuickTarget(TeleportLocationSource source, TeleportTarget target) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PacketSetQuickTarget> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MekanismTPforWaystones.MODID, "set_quick_target"));

    public static final StreamCodec<ByteBuf, PacketSetQuickTarget> STREAM_CODEC = StreamCodec.composite(
            TeleportLocationSource.STREAM_CODEC,
            PacketSetQuickTarget::source,
            TeleportTarget.STREAM_CODEC,
            PacketSetQuickTarget::target,
            PacketSetQuickTarget::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketSetQuickTarget packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                ItemStack stack = packet.source().findItem(serverPlayer);
                if (!stack.isEmpty()) {
                    stack.set(ModDataComponents.QUICK_TARGET.get(), packet.target());
                    serverPlayer.sendSystemMessage(
                            Component.translatable("message.mekanism_waystones.quick_target_set", packet.target().name())
                                    .withStyle(ChatFormatting.AQUA)
                    );
                }
            }
        });
    }
}
