package net.kafgchan.mekanism_waystones.item;

import net.kafgchan.mekanism_waystones.menu.PortableTeleporterPlusMenu;
import net.kafgchan.mekanism_waystones.target.PlayerFavoritesService;
import net.kafgchan.mekanism_waystones.target.TeleportDestinationService;
import net.kafgchan.mekanism_waystones.target.TeleportLocationSource;
import net.kafgchan.mekanism_waystones.target.TeleportTarget;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 使い捨てテレポーター (Disposable Teleporter - Forge 1.20.1)
 * 1回限りの使い捨て型テレポート端末。
 * - 所有者登録不要。最大16個スタック可能。
 * - Mekanism公開テレポーターおよびWaystonesへテレポート可能。
 * - テレポート成功時にアイテムを1個消費。
 */
public class ItemDisposableTeleporter extends Item {

    public ItemDisposableTeleporter(Properties properties) {
        super(properties.stacksTo(16).rarity(Rarity.UNCOMMON));
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.mekanism_waystones.disposable_teleporter.desc")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.mekanism_waystones.disposable_teleporter.single_use")
                .withStyle(ChatFormatting.GOLD));
    }

    public static void openGui(ServerPlayer serverPlayer, ItemStack stack, TeleportLocationSource source) {
        // 公開先のみ取得 (isDisposable = true)
        List<TeleportTarget> destinations = TeleportDestinationService.getAvailableTargets(serverPlayer, true);
        Set<String> favorites = PlayerFavoritesService.getFavorites(serverPlayer);

        NetworkHooks.openScreen(
                serverPlayer,
                new SimpleMenuProvider(
                        (id, inv, p) -> new PortableTeleporterPlusMenu(id, inv, source, destinations, 0, null, new ArrayList<>(favorites), true),
                        Component.translatable("item.mekanism_waystones.disposable_teleporter")
                ),
                buf -> {
                    source.encode(buf);
                    buf.writeVarInt(0); // energy = 0
                    buf.writeBoolean(true); // isDisposable = true
                    buf.writeBoolean(false); // hasQuickTarget = false
                    buf.writeVarInt(favorites.size());
                    for (String fav : favorites) {
                        buf.writeUtf(fav);
                    }
                    buf.writeVarInt(destinations.size());
                    for (TeleportTarget dest : destinations) {
                        dest.encode(buf);
                    }
                }
        );
    }

    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            if (player instanceof ServerPlayer serverPlayer) {
                openGui(serverPlayer, stack, TeleportLocationSource.ofHand(hand));
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
