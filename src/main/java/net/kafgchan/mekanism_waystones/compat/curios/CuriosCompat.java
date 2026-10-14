package net.kafgchan.mekanism_waystones.compat.curios;

import net.kafgchan.mekanism_waystones.config.TeleporterConfig;
import net.kafgchan.mekanism_waystones.target.TeleportLocationSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Curios API とのソフト依存（オプショナル）連携クラス (NeoForge 1.21.1)。
 */
public class CuriosCompat {

    public static final String CURIOS_MODID = "curios";

    public static boolean isLoaded() {
        return ModList.get().isLoaded(CURIOS_MODID) && TeleporterConfig.isCuriosEnabled();
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        if (ModList.get().isLoaded(CURIOS_MODID)) {
            CuriosInternalHandler.registerCapabilities(event);
        }
    }

    @NotNull
    public static ItemStack getItem(Player player, String identifier, int slotIndex) {
        if (!isLoaded()) {
            return ItemStack.EMPTY;
        }
        return CuriosInternalHandler.getItem(player, identifier, slotIndex);
    }

    public static void setItem(Player player, String identifier, int slotIndex, ItemStack stack) {
        if (isLoaded()) {
            CuriosInternalHandler.setItem(player, identifier, slotIndex, stack);
        }
    }

    public static Optional<TeleportLocationSource> findTeleporterInCurios(Player player) {
        if (!isLoaded()) {
            return Optional.empty();
        }
        return CuriosInternalHandler.findTeleporter(player);
    }

    public static Optional<TeleportLocationSource> findMatchingCurio(Player player, java.util.function.Predicate<ItemStack> filter) {
        if (!isLoaded()) {
            return Optional.empty();
        }
        return CuriosInternalHandler.findMatchingCurio(player, filter);
    }
}
