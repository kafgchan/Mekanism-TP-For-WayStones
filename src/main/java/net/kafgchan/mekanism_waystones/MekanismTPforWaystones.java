package net.kafgchan.mekanism_waystones;

import com.mojang.logging.LogUtils;
import net.kafgchan.mekanism_waystones.item.ItemPortableTeleporterPlus;
import net.kafgchan.mekanism_waystones.registration.ModCreativeTabs;
import net.kafgchan.mekanism_waystones.registration.ModDataComponents;
import net.kafgchan.mekanism_waystones.registration.ModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.ComponentEnergyStorage;
import org.slf4j.Logger;

@Mod(MekanismTPforWaystones.MODID)
public class MekanismTPforWaystones {
    public static final String MODID = "mekanism_waystones";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MekanismTPforWaystones(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerCapabilities);
        modEventBus.addListener(net.kafgchan.mekanism_waystones.network.ModPayloads::register);

        // Register registries
        ModDataComponents.register(modEventBus);
        ModItems.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        net.kafgchan.mekanism_waystones.registration.ModMenuTypes.register(modEventBus);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Mekanism: TP for Waystones initialized.");
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerItem(
                Capabilities.EnergyStorage.ITEM,
                (stack, ctx) -> new ComponentEnergyStorage(
                        stack,
                        ModDataComponents.ENERGY.get(),
                        ItemPortableTeleporterPlus.MAX_ENERGY,
                        ItemPortableTeleporterPlus.MAX_TRANSFER
                ),
                ModItems.PORTABLE_TELEPORTER_PLUS
        );
    }
}
