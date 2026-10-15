package net.kafgchan.mekanism_waystones.menu;

import net.kafgchan.mekanism_waystones.registration.ModItems;
import net.kafgchan.mekanism_waystones.registration.ModMenuTypes;
import net.kafgchan.mekanism_waystones.target.TeleportLocationSource;
import net.kafgchan.mekanism_waystones.target.TeleportTarget;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * テレポート端末の操作メニュー。
 * v1.1.0: プレイヤーインベントリ表示（スロット）を廃止し、GUI領域を一覧表示へ最適化。
 */
public class PortableTeleporterPlusMenu extends AbstractContainerMenu {
    private final TeleportLocationSource source;
    private final List<TeleportTarget> targets;
    private final int energy;
    private final TeleportTarget quickTarget;
    private final List<String> favorites;
    private final boolean isDisposable;

    public PortableTeleporterPlusMenu(
            int containerId,
            Inventory playerInventory,
            TeleportLocationSource source,
            List<TeleportTarget> targets,
            int energy,
            @Nullable TeleportTarget quickTarget,
            List<String> favorites,
            boolean isDisposable
    ) {
        super(ModMenuTypes.PORTABLE_TELEPORTER_PLUS.get(), containerId);
        this.source = source;
        this.targets = targets != null ? List.copyOf(targets) : Collections.emptyList();
        this.energy = energy;
        this.quickTarget = quickTarget;
        this.favorites = favorites != null ? List.copyOf(favorites) : Collections.emptyList();
        this.isDisposable = isDisposable;
    }

    public TeleportLocationSource getSource() {
        return source;
    }

    public List<TeleportTarget> getTargets() {
        return targets;
    }

    public int getEnergy() {
        return energy;
    }

    @Nullable
    public TeleportTarget getQuickTarget() {
        return quickTarget;
    }

    public List<String> getFavorites() {
        return favorites;
    }

    public boolean isDisposable() {
        return isDisposable;
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
        if (held.isEmpty()) {
            return false;
        }
        if (isDisposable) {
            return held.is(ModItems.DISPOSABLE_TELEPORTER.get());
        }
        return held.is(ModItems.PORTABLE_TELEPORTER_PLUS.get());
    }
}
