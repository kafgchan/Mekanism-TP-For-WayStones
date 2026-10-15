package net.kafgchan.mekanism_waystones.registration;

import net.kafgchan.mekanism_waystones.MekanismTPforWaystones;
import net.kafgchan.mekanism_waystones.item.ItemDisposableBattery;
import net.kafgchan.mekanism_waystones.item.ItemDisposableTeleporter;
import net.kafgchan.mekanism_waystones.item.ItemPortableTeleporterPlus;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MekanismTPforWaystones.MODID);

    public static final RegistryObject<ItemPortableTeleporterPlus> PORTABLE_TELEPORTER_PLUS =
            ITEMS.register("portable_teleporter_plus", () -> new ItemPortableTeleporterPlus(new Item.Properties()));

    public static final RegistryObject<ItemDisposableBattery> DISPOSABLE_BATTERY =
            ITEMS.register("disposable_battery", () -> new ItemDisposableBattery(new Item.Properties()));

    public static final RegistryObject<ItemDisposableTeleporter> DISPOSABLE_TELEPORTER =
            ITEMS.register("disposable_teleporter", () -> new ItemDisposableTeleporter(new Item.Properties().stacksTo(16)));


    private ModItems() {
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}

