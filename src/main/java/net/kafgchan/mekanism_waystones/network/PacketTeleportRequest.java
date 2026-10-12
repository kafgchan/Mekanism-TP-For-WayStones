package net.kafgchan.mekanism_waystones.network;

import io.netty.buffer.ByteBuf;
import net.kafgchan.mekanism_waystones.MekanismTPforWaystones;
import net.kafgchan.mekanism_waystones.target.TeleportExecutionService;
import net.kafgchan.mekanism_waystones.target.TeleportTarget;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketTeleportRequest(InteractionHand hand, TeleportTarget target) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PacketTeleportRequest> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MekanismTPforWaystones.MODID, "teleport_request"));

    private static final StreamCodec<ByteBuf, InteractionHand> HAND_STREAM_CODEC =
            ByteBufCodecs.BOOL.map(b -> b ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND, h -> h == InteractionHand.MAIN_HAND);

    public static final StreamCodec<ByteBuf, PacketTeleportRequest> STREAM_CODEC = StreamCodec.composite(
            HAND_STREAM_CODEC,
            PacketTeleportRequest::hand,
            TeleportTarget.STREAM_CODEC,
            PacketTeleportRequest::target,
            PacketTeleportRequest::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketTeleportRequest packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                ItemStack stack = serverPlayer.getItemInHand(packet.hand());
                TeleportExecutionService.Result result = TeleportExecutionService.executeTeleport(serverPlayer, stack, packet.target());
                if (result == TeleportExecutionService.Result.SUCCESS) {
                    serverPlayer.closeContainer();
                } else {
                    net.minecraft.network.chat.Component msg = switch (result) {
                        case NOT_OWNER -> net.minecraft.network.chat.Component.translatable("message.mekanism_waystones.not_owner_generic");
                        case INSUFFICIENT_ENERGY -> net.minecraft.network.chat.Component.translatable("message.mekanism_waystones.insufficient_energy");
                        case TARGET_NOT_FOUND -> net.minecraft.network.chat.Component.translatable("message.mekanism_waystones.target_not_found");
                        case UNSAFE_DESTINATION -> net.minecraft.network.chat.Component.translatable("message.mekanism_waystones.unsafe_destination");
                        default -> net.minecraft.network.chat.Component.translatable("message.mekanism_waystones.teleport_failed");
                    };
                    serverPlayer.sendSystemMessage(msg.copy().withStyle(net.minecraft.ChatFormatting.RED));
                }
            }
        });
    }
}
