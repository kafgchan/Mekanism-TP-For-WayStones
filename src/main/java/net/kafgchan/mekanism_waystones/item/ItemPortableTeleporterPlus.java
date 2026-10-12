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

        tooltip.add(Component.translatable("tooltip.mekanism_waystones.portable_teleporter_plus.desc")
                .withStyle(ChatFormatting.DARK_GRAY));
    }

    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            TeleporterOwner owner = stack.get(ModDataComponents.OWNER.get());
            if (owner == null) {
                // 初回使用時: 使用者のUUIDと名前を所有者として登録
                TeleporterOwner newOwner = new TeleporterOwner(player.getUUID(), player.getName().getString());
                stack.set(ModDataComponents.OWNER.get(), newOwner);
                player.sendSystemMessage(Component.translatable("message.mekanism_waystones.owner_registered", player.getName().getString())
                        .withStyle(ChatFormatting.GREEN));
            } else if (!owner.isOwner(player.getUUID())) {
                // 登録された所有者と異なるプレイヤーによる操作を拒否
                player.sendSystemMessage(Component.translatable("message.mekanism_waystones.not_owner", owner.name())
                        .withStyle(ChatFormatting.RED));
                return InteractionResultHolder.fail(stack);
            } else {
                // 所有者一致
            }

            if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                java.util.List<net.kafgchan.mekanism_waystones.target.TeleportTarget> destinations =
                        net.kafgchan.mekanism_waystones.target.TeleportDestinationService.getAvailableTargets(serverPlayer);
                int currentEnergy = getEnergy(stack);

                serverPlayer.openMenu(
                        new net.minecraft.world.SimpleMenuProvider(
                                (id, inv, p) -> new net.kafgchan.mekanism_waystones.menu.PortableTeleporterPlusMenu(id, inv, hand, destinations, currentEnergy),
                                net.minecraft.network.chat.Component.translatable("item.mekanism_waystones.portable_teleporter_plus")
                        ),
                        buf -> {
                            buf.writeBoolean(hand == net.minecraft.world.InteractionHand.MAIN_HAND);
                            buf.writeVarInt(currentEnergy);
                            net.kafgchan.mekanism_waystones.target.TeleportTarget.STREAM_CODEC
                                    .apply(net.minecraft.network.codec.ByteBufCodecs.list())
                                    .encode(buf, destinations);
                        }
                );
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
