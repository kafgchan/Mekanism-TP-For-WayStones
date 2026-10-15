package net.kafgchan.mekanism_waystones.item;

import net.kafgchan.mekanism_waystones.registration.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Portable Teleporter Plus
 * Mekanism のテレポート先と Waystones のワープ先を統合管理する携帯テレポート端末。
 * - 初回使用者のUUIDを所有者として永続化。
 * - 基本エネルギー容量 800,000 FE (800 kFE)。
 */
public class ItemPortableTeleporterPlus extends Item {

    public static final int MAX_ENERGY = 800_000;
    public static final int MAX_TRANSFER = 800_000;
    private static final int ENERGY_BAR_COLOR = 0x38D648; // Forge Energy グリーン (FE)

    public static int getMaxEnergy() {
        return net.kafgchan.mekanism_waystones.config.TeleporterConfig.getMaxEnergy();
    }

    public static int getMaxTransfer() {
        return net.kafgchan.mekanism_waystones.config.TeleporterConfig.getMaxTransfer();
    }

    public ItemPortableTeleporterPlus(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.RARE));
    }

    public static int getEnergy(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.ENERGY.get(), 0);
    }

    public static void setEnergy(ItemStack stack, int energy) {
        stack.set(ModDataComponents.ENERGY.get(), Math.clamp(energy, 0, getMaxEnergy()));
    }

    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(@NotNull ItemStack stack) {
        int max = getMaxEnergy();
        return max > 0 ? Math.round(13.0F * getEnergy(stack) / max) : 0;
    }

    @Override
    public int getBarColor(@NotNull ItemStack stack) {
        return ENERGY_BAR_COLOR;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);

        // 所有者情報
        TeleporterOwner owner = stack.get(ModDataComponents.OWNER.get());
        if (owner != null) {
            tooltip.add(Component.translatable("tooltip.mekanism_waystones.owner", owner.name())
                    .withStyle(ChatFormatting.AQUA));
        } else {
            tooltip.add(Component.translatable("tooltip.mekanism_waystones.no_owner")
                    .withStyle(ChatFormatting.GRAY));
        }

        // エネルギー情報
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        int currentEnergy = getEnergy(stack);
        tooltip.add(Component.translatable("tooltip.mekanism_waystones.energy",
                nf.format(currentEnergy), nf.format(getMaxEnergy()))
                .withStyle(ChatFormatting.GREEN));

        // クイックテレポート先
        net.kafgchan.mekanism_waystones.target.TeleportTarget quickTarget = stack.get(ModDataComponents.QUICK_TARGET.get());
        if (quickTarget != null) {
            tooltip.add(Component.translatable("tooltip.mekanism_waystones.quick_target", quickTarget.name())
                    .withStyle(ChatFormatting.GOLD));
        }

        tooltip.add(Component.translatable("tooltip.mekanism_waystones.portable_teleporter_plus.desc")
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("tooltip.mekanism_waystones.portable_teleporter_plus.quick_hint")
                .withStyle(ChatFormatting.GRAY));
    }

    public static boolean checkOrSetOwner(Player player, ItemStack stack) {
        TeleporterOwner owner = stack.get(ModDataComponents.OWNER.get());
        if (owner == null) {
            // 初回使用時: 使用者のUUIDと名前を所有者として登録
            TeleporterOwner newOwner = new TeleporterOwner(player.getUUID(), player.getName().getString());
            stack.set(ModDataComponents.OWNER.get(), newOwner);
            player.sendSystemMessage(Component.translatable("message.mekanism_waystones.owner_registered", player.getName().getString())
                    .withStyle(ChatFormatting.GREEN));
            return true;
        } else if (!owner.isOwner(player.getUUID())) {
            // 登録された所有者と異なるプレイヤーによる操作を拒否
            player.sendSystemMessage(Component.translatable("message.mekanism_waystones.not_owner", owner.name())
                    .withStyle(ChatFormatting.RED));
            return false;
        }
        return true;
    }

    public static void openGui(net.minecraft.server.level.ServerPlayer serverPlayer, ItemStack stack, net.kafgchan.mekanism_waystones.target.TeleportLocationSource source) {
        if (!checkOrSetOwner(serverPlayer, stack)) {
            return;
        }
        java.util.List<net.kafgchan.mekanism_waystones.target.TeleportTarget> destinations =
                net.kafgchan.mekanism_waystones.target.TeleportDestinationService.getAvailableTargets(serverPlayer, false);
        int currentEnergy = getEnergy(stack);
        net.kafgchan.mekanism_waystones.target.TeleportTarget quickTarget = stack.get(ModDataComponents.QUICK_TARGET.get());
        java.util.Set<String> favorites = net.kafgchan.mekanism_waystones.target.PlayerFavoritesService.getFavorites(serverPlayer);

        serverPlayer.openMenu(
                new net.minecraft.world.SimpleMenuProvider(
                        (id, inv, p) -> new net.kafgchan.mekanism_waystones.menu.PortableTeleporterPlusMenu(id, inv, source, destinations, currentEnergy, quickTarget, new java.util.ArrayList<>(favorites), false),
                        net.minecraft.network.chat.Component.translatable("item.mekanism_waystones.portable_teleporter_plus")
                ),
                buf -> {
                    net.kafgchan.mekanism_waystones.target.TeleportLocationSource.STREAM_CODEC.encode(buf, source);
                    buf.writeVarInt(currentEnergy);
                    buf.writeBoolean(false); // isDisposable = false
                    if (quickTarget != null) {
                        buf.writeBoolean(true);
                        net.kafgchan.mekanism_waystones.target.TeleportTarget.STREAM_CODEC.encode(buf, quickTarget);
                    } else {
                        buf.writeBoolean(false);
                    }
                    net.minecraft.network.codec.ByteBufCodecs.STRING_UTF8
                            .apply(net.minecraft.network.codec.ByteBufCodecs.list())
                            .encode(buf, new java.util.ArrayList<>(favorites));
                    net.kafgchan.mekanism_waystones.target.TeleportTarget.STREAM_CODEC
                            .apply(net.minecraft.network.codec.ByteBufCodecs.list())
                            .encode(buf, destinations);
                }
        );
    }

    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                if (serverPlayer.isShiftKeyDown()) {
                    // Shift + 右クリック: クイックテレポート
                    executeQuickTeleport(serverPlayer, stack, net.kafgchan.mekanism_waystones.target.TeleportLocationSource.ofHand(hand));
                } else {
                    // 通常右クリック: GUI起動
                    openGui(serverPlayer, stack, net.kafgchan.mekanism_waystones.target.TeleportLocationSource.ofHand(hand));
                }
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    private static void executeQuickTeleport(net.minecraft.server.level.ServerPlayer serverPlayer, ItemStack stack, net.kafgchan.mekanism_waystones.target.TeleportLocationSource source) {
        if (!checkOrSetOwner(serverPlayer, stack)) {
            return;
        }

        net.kafgchan.mekanism_waystones.target.TeleportTarget quickTarget = stack.get(ModDataComponents.QUICK_TARGET.get());
        if (quickTarget == null) {
            serverPlayer.sendSystemMessage(
                    Component.translatable("message.mekanism_waystones.quick_target_not_set")
                            .withStyle(ChatFormatting.RED)
            );
            return;
        }

        net.kafgchan.mekanism_waystones.target.TeleportExecutionService.Result result =
                net.kafgchan.mekanism_waystones.target.TeleportExecutionService.executeTeleport(serverPlayer, stack, quickTarget);

        if (result != net.kafgchan.mekanism_waystones.target.TeleportExecutionService.Result.SUCCESS) {
            Component msg = switch (result) {
                case NOT_OWNER -> Component.translatable("message.mekanism_waystones.not_owner_generic");
                case INSUFFICIENT_ENERGY -> Component.translatable("message.mekanism_waystones.insufficient_energy");
                case TARGET_NOT_FOUND -> Component.translatable("message.mekanism_waystones.target_not_found");
                case UNSAFE_DESTINATION -> Component.translatable("message.mekanism_waystones.unsafe_destination");
                default -> Component.translatable("message.mekanism_waystones.teleport_failed");
            };
            serverPlayer.sendSystemMessage(msg.copy().withStyle(ChatFormatting.RED));
        }
    }

}
