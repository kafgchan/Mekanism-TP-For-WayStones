package net.kafgchan.mekanism_waystones.registration;

import net.kafgchan.mekanism_waystones.MekanismTPforWaystones;
import net.kafgchan.mekanism_waystones.item.ItemPortableTeleporterPlus;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MekanismTPforWaystones.MODID);

    public static final DeferredItem<ItemPortableTeleporterPlus> PORTABLE_TELEPORTER_PLUS =
            ITEMS.registerItem("portable_teleporter_plus", ItemPortableTeleporterPlus::new);

    private ModItems() {
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
