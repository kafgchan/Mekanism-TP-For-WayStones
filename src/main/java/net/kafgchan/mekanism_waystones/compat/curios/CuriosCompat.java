package net.kafgchan.mekanism_waystones.compat.curios;

import net.kafgchan.mekanism_waystones.config.TeleporterConfig;
import net.kafgchan.mekanism_waystones.item.ItemPortableTeleporterPlus;
import net.kafgchan.mekanism_waystones.target.TeleportLocationSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Curios API とのソフト依存（オプショナル）連携クラス。
 * Curios API が未導入の環境でもクラスローディングエラーを起こさないよう、
 * 実際の Curios クラス参照は {@link CuriosInternalHandler} に委譲します。
 */
public class CuriosCompat {

    public static final String CURIOS_MODID = "curios";

    /**
     * Curios MOD がロードされており、かつ設定で有効化されているかを返します。
     */
    public static boolean isLoaded() {
        return ModList.get().isLoaded(CURIOS_MODID) && TeleporterConfig.isCuriosEnabled();
    }

    /**
     * アイテム用の統合 CapabilityProvider (FE + Curios) を生成します。
     */
    public static ICapabilityProvider createCapabilityProvider(ItemStack stack) {
        if (isLoaded()) {
            return CuriosInternalHandler.createProvider(stack);
        }
        return new ItemPortableTeleporterPlus.EnergyCapabilityProvider(stack);
    }

    /**
     * プレイヤーの Curios スロットから指定された識別子・インデックスのアイテムを取得します。
     */
    @NotNull
    public static ItemStack getItem(Player player, String identifier, int slotIndex) {
        if (!isLoaded()) {
            return ItemStack.EMPTY;
        }
        return CuriosInternalHandler.getItem(player, identifier, slotIndex);
    }

    /**
     * プレイヤーの Curios スロットに装備されている最初の Portable Teleporter Plus を探します。
     */
    public static Optional<TeleportLocationSource> findTeleporterInCurios(Player player) {
        if (!isLoaded()) {
            return Optional.empty();
        }
        return CuriosInternalHandler.findTeleporter(player);
    }

    public static void setItem(Player player, String identifier, int slotIndex, ItemStack stack) {
        if (isLoaded()) {
            CuriosInternalHandler.setItem(player, identifier, slotIndex, stack);
        }
    }

    public static Optional<TeleportLocationSource> findMatchingCurio(Player player, java.util.function.Predicate<ItemStack> filter) {
        if (!isLoaded()) {
            return Optional.empty();
        }
        return CuriosInternalHandler.findMatchingCurio(player, filter);
    }
}
