package net.kafgchan.mekanism_waystones.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * 使い捨て電池 (NeoForge 1.21.1)
 * 25,000 FE相当のエネルギーが封入された中間素材。
 * - 常時満タンのForge Energyバーを表示。
 * - 最大16個スタック可能。
 */
public class ItemDisposableBattery extends Item {
    public static final int CAPACITY = 25_000;
    private static final int ENERGY_BAR_COLOR = 0x38D648; // Forge Energy グリーン (FE)

    public ItemDisposableBattery(Properties properties) {
        super(properties.stacksTo(16));
    }

    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(@NotNull ItemStack stack) {
        return 13; // 満タン表示 (13px)
    }

    @Override
    public int getBarColor(@NotNull ItemStack stack) {
        return ENERGY_BAR_COLOR;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);

        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        tooltip.add(Component.translatable("tooltip.mekanism_waystones.energy",
                nf.format(CAPACITY), nf.format(CAPACITY))
                .withStyle(ChatFormatting.GREEN));

        tooltip.add(Component.translatable("tooltip.mekanism_waystones.disposable_battery.desc")
                .withStyle(ChatFormatting.GRAY));
    }
}
