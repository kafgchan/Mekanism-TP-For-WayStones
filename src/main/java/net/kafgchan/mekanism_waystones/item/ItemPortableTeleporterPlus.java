package net.kafgchan.mekanism_waystones.item;

import net.kafgchan.mekanism_waystones.compat.curios.CuriosCompat;
import net.kafgchan.mekanism_waystones.config.TeleporterConfig;
import net.kafgchan.mekanism_waystones.menu.PortableTeleporterPlusMenu;
import net.kafgchan.mekanism_waystones.target.TeleportDestinationService;
import net.kafgchan.mekanism_waystones.target.TeleportLocationSource;
import net.kafgchan.mekanism_waystones.target.TeleportTarget;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Portable Teleporter Plus
 * Mekanism のテレポート先と Waystones のワープ先を統合管理する携帯テレポート端末。
 * - 初回使用者のUUIDを所有者として永続化。
 * - 基本エネルギー容量 800,000 FE (800 kFE)。
 * - Forge Energy (FE) 完全互換 (Mekanism 充電器対応)。
 */
public class ItemPortableTeleporterPlus extends Item {

    public static final int MAX_ENERGY = 800_000;
    public static final int MAX_TRANSFER = 800_000;
    private static final int ENERGY_BAR_COLOR = 0x38D648; // Forge Energy グリーン (FE)

    public static int getMaxEnergy() {
        return TeleporterConfig.getMaxEnergy();
    }

    public static int getMaxTransfer() {
        return TeleporterConfig.getMaxTransfer();
    }

    public ItemPortableTeleporterPlus(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.RARE));
    }

    public static int getEnergy(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null ? tag.getInt("energy") : 0;
    }

    public static void setEnergy(ItemStack stack, int energy) {
        int clamped = Math.max(0, Math.min(energy, getMaxEnergy()));
        stack.getOrCreateTag().putInt("energy", clamped);
    }

    @Nullable
    public static TeleporterOwner getOwner(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("owner")) {
            return TeleporterOwner.fromNbt(tag.getCompound("owner"));
        }
        return null;
    }

    public static void setOwner(ItemStack stack, TeleporterOwner owner) {
        if (owner != null) {
            stack.getOrCreateTag().put("owner", owner.saveToNbt());
        } else if (stack.hasTag()) {
            stack.getTag().remove("owner");
        }
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
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        // 所有者情報
        TeleporterOwner owner = getOwner(stack);
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

    public static boolean checkOrSetOwner(ServerPlayer player, ItemStack stack) {
        TeleporterOwner owner = getOwner(stack);
        if (owner == null) {
            // 初回使用時: 使用者のUUIDと名前を所有者として登録
            TeleporterOwner newOwner = new TeleporterOwner(player.getUUID(), player.getName().getString());
            setOwner(stack, newOwner);
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

    public static void openGui(ServerPlayer serverPlayer, ItemStack stack, TeleportLocationSource source) {
        if (!checkOrSetOwner(serverPlayer, stack)) {
            return;
        }
        List<TeleportTarget> destinations = TeleportDestinationService.getAvailableTargets(serverPlayer);
        int currentEnergy = getEnergy(stack);

        NetworkHooks.openScreen(
                serverPlayer,
                new SimpleMenuProvider(
                        (id, inv, p) -> new PortableTeleporterPlusMenu(id, inv, source, destinations, currentEnergy),
                        Component.translatable("item.mekanism_waystones.portable_teleporter_plus")
                ),
                buf -> {
                    source.encode(buf);
                    buf.writeVarInt(currentEnergy);
                    buf.writeVarInt(destinations.size());
                    for (TeleportTarget target : destinations) {
                        target.encode(buf);
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

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return CuriosCompat.createCapabilityProvider(stack);
    }

    /**
     * Forge Energy Capability Provider
     * Mekanism 充電器や FE 機器との完全な連携を提供
     */
    public static class EnergyCapabilityProvider implements ICapabilityProvider, IEnergyStorage {
        private final ItemStack stack;
        private final LazyOptional<IEnergyStorage> holder = LazyOptional.of(() -> this);

        public EnergyCapabilityProvider(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            if (cap == ForgeCapabilities.ENERGY) {
                return holder.cast();
            }
            return LazyOptional.empty();
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int current = getEnergy(stack);
            int max = getMaxEnergy();
            int transfer = getMaxTransfer();
            int toReceive = Math.min(max - current, Math.min(maxReceive, transfer));
            if (toReceive <= 0) {
                return 0;
            }
            if (!simulate) {
                setEnergy(stack, current + toReceive);
            }
            return toReceive;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            int current = getEnergy(stack);
            int transfer = getMaxTransfer();
            int toExtract = Math.min(current, Math.min(maxExtract, transfer));
            if (toExtract <= 0) {
                return 0;
            }
            if (!simulate) {
                setEnergy(stack, current - toExtract);
            }
            return toExtract;
        }

        @Override
        public int getEnergyStored() {
            return getEnergy(stack);
        }

        @Override
        public int getMaxEnergyStored() {
            return getMaxEnergy();
        }

        @Override
        public boolean canExtract() {
            return true;
        }

        @Override
        public boolean canReceive() {
            return true;
        }
    }
}
