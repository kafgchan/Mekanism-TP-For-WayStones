package net.kafgchan.mekanism_waystones.network;

import net.kafgchan.mekanism_waystones.compat.curios.CuriosCompat;
import net.kafgchan.mekanism_waystones.config.TeleporterConfig;
import net.kafgchan.mekanism_waystones.item.ItemPortableTeleporterPlus;
import net.kafgchan.mekanism_waystones.registration.ModItems;
import net.kafgchan.mekanism_waystones.target.TeleportLocationSource;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * クライアントのショートカットキー押下時にサーバーへテレポート端末GUI起動を要求するパケット。
 */
public class PacketOpenTeleporterRequest {

    public PacketOpenTeleporterRequest() {
    }

    public static void encode(PacketOpenTeleporterRequest msg, FriendlyByteBuf buf) {
    }

    public static PacketOpenTeleporterRequest decode(FriendlyByteBuf buf) {
        return new PacketOpenTeleporterRequest();
    }

    public static void handle(PacketOpenTeleporterRequest msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null || !TeleporterConfig.isKeybindEnabled()) {
                return;
            }

            // 1. Curios スロット探索 (Curios 有効時)
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

            // 4. Mekanism 純正 Portable Teleporter 探索 (Plus版が見つからなかった場合)
            // 4-1. Curios スロット探索
            if (CuriosCompat.isLoaded()) {
                Optional<TeleportLocationSource> mekCurios = CuriosCompat.findMatchingCurio(player, net.kafgchan.mekanism_waystones.compat.mekanism.MekanismTeleporterHelper::isMekanismTeleporter);
                if (mekCurios.isPresent()) {
                    TeleportLocationSource source = mekCurios.get();
                    ItemStack stack = source.findItem(player);
                    if (!stack.isEmpty()) {
                        net.kafgchan.mekanism_waystones.compat.mekanism.MekanismTeleporterHelper.openMekanismGui(player, stack, source);
                        return;
                    }
                }
            }

            // 4-2. 手持ち探索 (メインハンド -> オフハンド)
            if (net.kafgchan.mekanism_waystones.compat.mekanism.MekanismTeleporterHelper.isMekanismTeleporter(mainHand)) {
                net.kafgchan.mekanism_waystones.compat.mekanism.MekanismTeleporterHelper.openMekanismGui(player, mainHand, TeleportLocationSource.ofHand(InteractionHand.MAIN_HAND));
                return;
            }
            if (net.kafgchan.mekanism_waystones.compat.mekanism.MekanismTeleporterHelper.isMekanismTeleporter(offHand)) {
                net.kafgchan.mekanism_waystones.compat.mekanism.MekanismTeleporterHelper.openMekanismGui(player, offHand, TeleportLocationSource.ofHand(InteractionHand.OFF_HAND));
                return;
            }

            // 4-3. インベントリ探索 (設定で許可されている場合)
            if (TeleporterConfig.isKeybindSearchInventoryEnabled()) {
                for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                    ItemStack invStack = player.getInventory().getItem(i);
                    if (net.kafgchan.mekanism_waystones.compat.mekanism.MekanismTeleporterHelper.isMekanismTeleporter(invStack)) {
                        net.kafgchan.mekanism_waystones.compat.mekanism.MekanismTeleporterHelper.openMekanismGui(player, invStack, TeleportLocationSource.ofInventory(i));
                        return;
                    }
                }
            }

            // 見つからなかった場合の通知
            player.sendSystemMessage(Component.translatable("message.mekanism_waystones.teleporter_not_found")
                    .withStyle(ChatFormatting.RED));
        });
        ctx.setPacketHandled(true);
    }
}
