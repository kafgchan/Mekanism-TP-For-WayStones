package net.kafgchan.mekanism_waystones.network;

import io.netty.buffer.ByteBuf;
import net.kafgchan.mekanism_waystones.MekanismTPforWaystones;
import net.kafgchan.mekanism_waystones.compat.curios.CuriosCompat;
import net.kafgchan.mekanism_waystones.config.TeleporterConfig;
import net.kafgchan.mekanism_waystones.item.ItemPortableTeleporterPlus;
import net.kafgchan.mekanism_waystones.registration.ModItems;
import net.kafgchan.mekanism_waystones.target.TeleportLocationSource;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Optional;

/**
 * クライアントのショートカットキー押下時にサーバーへテレポート端末GUI起動を要求するパケット (NeoForge 1.21.1)。
 */
public record PacketOpenTeleporterRequest() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PacketOpenTeleporterRequest> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MekanismTPforWaystones.MODID, "open_teleporter_request"));

    public static final StreamCodec<ByteBuf, PacketOpenTeleporterRequest> STREAM_CODEC =
            StreamCodec.unit(new PacketOpenTeleporterRequest());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketOpenTeleporterRequest packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player) || !TeleporterConfig.isKeybindEnabled()) {
                return;
            }

            // 1. Curios スロット探索
            if (CuriosCompat.isLoaded()) {
                Optional<TeleportLocationSource> curiosSource = CuriosCompat.findTeleporterInCurios(player);
                if (curiosSource.isPresent()) {
                    TeleportLocationSource source = curiosSource.get();
                    ItemStack stack = source.findItem(player);
                    if (!stack.isEmpty()) {
                        ItemPortableTeleporterPlus.openGui(player, stack, source);
                        return;
                    }
                }
            }

            // 2. 手持ち探索 (メインハンド -> オフハンド)
            ItemStack mainHand = player.getItemInHand(InteractionHand.MAIN_HAND);
            if (!mainHand.isEmpty() && mainHand.is(ModItems.PORTABLE_TELEPORTER_PLUS.get())) {
                ItemPortableTeleporterPlus.openGui(player, mainHand, TeleportLocationSource.ofHand(InteractionHand.MAIN_HAND));
                return;
            }

            ItemStack offHand = player.getItemInHand(InteractionHand.OFF_HAND);
            if (!offHand.isEmpty() && offHand.is(ModItems.PORTABLE_TELEPORTER_PLUS.get())) {
                ItemPortableTeleporterPlus.openGui(player, offHand, TeleportLocationSource.ofHand(InteractionHand.OFF_HAND));
                return;
            }

            // 3. インベントリ探索 (設定で許可されている場合)
            if (TeleporterConfig.isKeybindSearchInventoryEnabled()) {
                for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                    ItemStack invStack = player.getInventory().getItem(i);
                    if (!invStack.isEmpty() && invStack.is(ModItems.PORTABLE_TELEPORTER_PLUS.get())) {
                        ItemPortableTeleporterPlus.openGui(player, invStack, TeleportLocationSource.ofInventory(i));
                        return;
                    }
                }
            }

            // 見つからなかった場合の通知
            player.sendSystemMessage(Component.translatable("message.mekanism_waystones.teleporter_not_found")
                    .withStyle(ChatFormatting.RED));
        });
    }
}
