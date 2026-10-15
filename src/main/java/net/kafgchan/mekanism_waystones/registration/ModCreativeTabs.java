package net.kafgchan.mekanism_waystones.registration;

import net.kafgchan.mekanism_waystones.MekanismTPforWaystones;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MekanismTPforWaystones.MODID);

    public static final RegistryObject<CreativeModeTab> TAB =
            CREATIVE_MODE_TABS.register("tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.mekanism_waystones.tab"))
                    .icon(() -> ModItems.PORTABLE_TELEPORTER_PLUS.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.PORTABLE_TELEPORTER_PLUS.get());
                        output.accept(ModItems.DISPOSABLE_BATTERY.get());
                        output.accept(ModItems.DISPOSABLE_TELEPORTER.get());
                    })

                    .build());

    private ModCreativeTabs() {
    }

    public static void register(IEventBus modEventBus) {
        CREATIVE_MODE_TABS.register(modEventBus);
    }
}
