package net.kafgchan.mekanism_waystones.compat.mekanism;

import net.kafgchan.mekanism_waystones.registration.ModItems;
import net.kafgchan.mekanism_waystones.target.TeleportLocationSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mekanism 純正 Portable Teleporter (mekanism:portable_teleporter) の
 * ホットキー呼び出しおよび一時ハンドスワップを管理するヘルパークラス (Forge 1.20.1)。
 */
public class MekanismTeleporterHelper {

    private record SwapRecord(UUID playerUUID, InteractionHand hand, TeleportLocationSource originalSource) {}

    private static final Map<UUID, SwapRecord> ACTIVE_SWAPS = new ConcurrentHashMap<>();

    public static boolean isMekanismTeleporter(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        if (stack.is(ModItems.PORTABLE_TELEPORTER_PLUS.get())) {
            return false;
        }
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return id != null && "mekanism".equals(id.getNamespace()) && "portable_teleporter".equals(id.getPath());
    }

    public static void openMekanismGui(ServerPlayer player, ItemStack teleporterStack, TeleportLocationSource source) {
        if (source.getType() == TeleportLocationSource.SourceType.HAND) {
            // 手持ちの場合はスワップ不要で直接開く
            teleporterStack.use(player.level(), player, source.getHand());
            return;
        }

        // Curios またはインベントリにある場合: 一時的に空いている手（またはオフハンド）とスワップしてGUIを開く
        InteractionHand hand = player.getItemInHand(InteractionHand.OFF_HAND).isEmpty()
                ? InteractionHand.OFF_HAND
                : (player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);

        ItemStack originalHandStack = player.getItemInHand(hand).copy();

        source.setItem(player, originalHandStack);
        player.setItemInHand(hand, teleporterStack);
        player.inventoryMenu.broadcastChanges();

        ACTIVE_SWAPS.put(player.getUUID(), new SwapRecord(player.getUUID(), hand, source));

        teleporterStack.use(player.level(), player, hand);
    }

    public static void restoreSwap(ServerPlayer player) {
        SwapRecord record = ACTIVE_SWAPS.remove(player.getUUID());
        if (record != null) {
            ItemStack currentHandStack = player.getItemInHand(record.hand());
            ItemStack originalSourceStack = record.originalSource().findItem(player);

            record.originalSource().setItem(player, currentHandStack);
            player.setItemInHand(record.hand(), originalSourceStack);
            player.inventoryMenu.broadcastChanges();
        }
    }

    @SubscribeEvent
    public static void onContainerClose(PlayerContainerEvent.Close event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            restoreSwap(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            restoreSwap(player);
        }
    }
}
