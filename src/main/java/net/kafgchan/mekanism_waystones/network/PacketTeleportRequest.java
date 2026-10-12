package net.kafgchan.mekanism_waystones.network;

import net.kafgchan.mekanism_waystones.target.TeleportExecutionService;
import net.kafgchan.mekanism_waystones.target.TeleportTarget;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketTeleportRequest {
    private final InteractionHand hand;
    private final TeleportTarget target;

    public PacketTeleportRequest(InteractionHand hand, TeleportTarget target) {
        this.hand = hand;
        this.target = target;
    }

    public InteractionHand getHand() {
        return hand;
    }

    public TeleportTarget getTarget() {
        return target;
    }

    public static void encode(PacketTeleportRequest msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.hand == InteractionHand.MAIN_HAND);
        msg.target.encode(buf);
    }

    public static PacketTeleportRequest decode(FriendlyByteBuf buf) {
        InteractionHand hand = buf.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        TeleportTarget target = TeleportTarget.decode(buf);
        return new PacketTeleportRequest(hand, target);
    }

    public static void handle(PacketTeleportRequest msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer serverPlayer = ctx.getSender();
            if (serverPlayer != null) {
                ItemStack stack = serverPlayer.getItemInHand(msg.hand);
                TeleportExecutionService.Result result = TeleportExecutionService.executeTeleport(serverPlayer, stack, msg.target);
                if (result == TeleportExecutionService.Result.SUCCESS) {
                    serverPlayer.closeContainer();
                } else {
                    Component errorMsg = switch (result) {
                        case NOT_OWNER -> Component.translatable("message.mekanism_waystones.not_owner_generic");
                        case INSUFFICIENT_ENERGY -> Component.translatable("message.mekanism_waystones.insufficient_energy");
                        case TARGET_NOT_FOUND -> Component.translatable("message.mekanism_waystones.target_not_found");
                        case UNSAFE_DESTINATION -> Component.translatable("message.mekanism_waystones.unsafe_destination");
                        default -> Component.translatable("message.mekanism_waystones.teleport_failed");
                    };
                    serverPlayer.sendSystemMessage(errorMsg.copy().withStyle(ChatFormatting.RED));
                }
            }
        });
        ctx.setPacketHandled(true);
    }
}
