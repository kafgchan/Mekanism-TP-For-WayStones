package net.kafgchan.mekanism_waystones.menu;

import net.kafgchan.mekanism_waystones.registration.ModItems;
import net.kafgchan.mekanism_waystones.registration.ModMenuTypes;
import net.kafgchan.mekanism_waystones.target.TeleportLocationSource;
import net.kafgchan.mekanism_waystones.target.TeleportTarget;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class PortableTeleporterPlusMenu extends AbstractContainerMenu {
    private final TeleportLocationSource source;
    private final List<TeleportTarget> targets;
    private final int energy;

    public PortableTeleporterPlusMenu(int containerId, Inventory playerInventory, TeleportLocationSource source, List<TeleportTarget> targets, int energy) {
        super(ModMenuTypes.PORTABLE_TELEPORTER_PLUS.get(), containerId);
        this.source = source;
        this.targets = targets != null ? List.copyOf(targets) : Collections.emptyList();
        this.energy = energy;

        // Player Inventory slots (3 rows x 9 columns)
        // Positioned at bottom: startX = 44, startY = 138
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 44 + col * 18, 138 + row * 18));
            }
        }

        // Hotbar slots (1 row x 9 columns)
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 44 + col * 18, 196));
        }
    }

    public PortableTeleporterPlusMenu(int containerId, Inventory playerInventory, InteractionHand hand, List<TeleportTarget> targets, int energy) {
        this(containerId, playerInventory, TeleportLocationSource.ofHand(hand), targets, energy);
    }

    public TeleportLocationSource getSource() {
        return source;
    }

    public InteractionHand getHand() {
        return source.getHand();
    }

    public List<TeleportTarget> getTargets() {
        return targets;
    }

    public int getEnergy() {
        return energy;
    }

    @NotNull
    @Override
    public ItemStack quickMoveStack(@NotNull Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        if (!player.isAlive()) {
            return false;
        }
        ItemStack held = source.findItem(player);
        return !held.isEmpty() && held.is(ModItems.PORTABLE_TELEPORTER_PLUS.get());
    }
}
