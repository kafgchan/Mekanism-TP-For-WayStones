package net.kafgchan.mekanism_waystones.compat.curios;

import net.kafgchan.mekanism_waystones.item.ItemPortableTeleporterPlus;
import net.kafgchan.mekanism_waystones.registration.ModItems;
import net.kafgchan.mekanism_waystones.target.TeleportLocationSource;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.Map;
import java.util.Optional;

/**
 * Curios API の内部呼び出しを直接行うクラス。
 * Curios が導入されている場合のみロードされます。
 */
public class CuriosInternalHandler {

    public static ICapabilityProvider createProvider(ItemStack stack) {
        return new CombinedCurioEnergyProvider(stack);
    }

    @NotNull
    public static ItemStack getItem(Player player, String identifier, int slotIndex) {
        LazyOptional<ICuriosItemHandler> opt = CuriosApi.getCuriosInventory(player);
        if (opt.isPresent()) {
            ICuriosItemHandler handler = opt.orElse(null);
            if (handler != null) {
                Map<String, ICurioStacksHandler> curios = handler.getCurios();
                ICurioStacksHandler stacksHandler = curios.get(identifier);
                if (stacksHandler != null && slotIndex >= 0 && slotIndex < stacksHandler.getSlots()) {
                    return stacksHandler.getStacks().getStackInSlot(slotIndex);
                }
            }
        }
        return ItemStack.EMPTY;
    }

    public static Optional<TeleportLocationSource> findTeleporter(Player player) {
        LazyOptional<ICuriosItemHandler> opt = CuriosApi.getCuriosInventory(player);
        if (opt.isPresent()) {
            ICuriosItemHandler handler = opt.orElse(null);
            if (handler != null) {
                Optional<SlotResult> result = handler.findFirstCurio(stack -> stack.is(ModItems.PORTABLE_TELEPORTER_PLUS.get()));
                if (result.isPresent()) {
                    SlotResult sr = result.get();
                    return Optional.of(TeleportLocationSource.ofCurios(sr.slotContext().identifier(), sr.slotContext().index()));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Forge Energy と Curios ICurio の両方を提供する Capability Provider
     */
    private static class CombinedCurioEnergyProvider implements ICapabilityProvider, IEnergyStorage, ICurio {
        private final ItemStack stack;
        private final LazyOptional<IEnergyStorage> energyHolder = LazyOptional.of(() -> this);
        private final LazyOptional<ICurio> curioHolder = LazyOptional.of(() -> this);

        public CombinedCurioEnergyProvider(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            if (cap == ForgeCapabilities.ENERGY) {
                return energyHolder.cast();
            }
            if (cap == CuriosCapability.ITEM) {
                return curioHolder.cast();
            }
            return LazyOptional.empty();
        }

        // --- ICurio ---
        @Override
        public ItemStack getStack() {
            return stack;
        }

        // --- IEnergyStorage ---
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int current = ItemPortableTeleporterPlus.getEnergy(stack);
            int max = ItemPortableTeleporterPlus.getMaxEnergy();
            int transfer = ItemPortableTeleporterPlus.getMaxTransfer();
            int toReceive = Math.min(max - current, Math.min(maxReceive, transfer));
            if (toReceive <= 0) {
                return 0;
            }
            if (!simulate) {
                ItemPortableTeleporterPlus.setEnergy(stack, current + toReceive);
            }
            return toReceive;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            int current = ItemPortableTeleporterPlus.getEnergy(stack);
            int transfer = ItemPortableTeleporterPlus.getMaxTransfer();
            int toExtract = Math.min(current, Math.min(maxExtract, transfer));
            if (toExtract <= 0) {
                return 0;
            }
            if (!simulate) {
                ItemPortableTeleporterPlus.setEnergy(stack, current - toExtract);
            }
            return toExtract;
        }

        @Override
        public int getEnergyStored() {
            return ItemPortableTeleporterPlus.getEnergy(stack);
        }

        @Override
        public int getMaxEnergyStored() {
            return ItemPortableTeleporterPlus.getMaxEnergy();
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
