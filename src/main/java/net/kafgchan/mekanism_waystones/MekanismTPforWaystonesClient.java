package net.kafgchan.mekanism_waystones;

import net.kafgchan.mekanism_waystones.client.gui.PortableTeleporterPlusScreen;
import net.kafgchan.mekanism_waystones.registration.ModMenuTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@Mod(value = MekanismTPforWaystones.MODID, dist = Dist.CLIENT)
public class MekanismTPforWaystonesClient {

    public MekanismTPforWaystonesClient(IEventBus modEventBus, ModContainer container) {
        modEventBus.addListener(this::onClientSetup);
        modEventBus.addListener(this::registerScreens);
        net.kafgchan.mekanism_waystones.client.ModKeyMappings.init(modEventBus);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        MekanismTPforWaystones.LOGGER.info("Mekanism: TP for Waystones Client Initialized.");
    }

    private void registerScreens(RegisterMenuScreensEvent event) {
        event.register(
                ModMenuTypes.PORTABLE_TELEPORTER_PLUS.get(),
                PortableTeleporterPlusScreen::new
        );
    }
}
