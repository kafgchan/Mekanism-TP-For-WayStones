package net.kafgchan.mekanism_waystones;

import com.mojang.logging.LogUtils;
import net.kafgchan.mekanism_waystones.client.gui.PortableTeleporterPlusScreen;
import net.kafgchan.mekanism_waystones.config.TeleporterConfig;
import net.kafgchan.mekanism_waystones.network.ModNetwork;
import net.kafgchan.mekanism_waystones.registration.ModCreativeTabs;
import net.kafgchan.mekanism_waystones.registration.ModItems;
import net.kafgchan.mekanism_waystones.registration.ModMenuTypes;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(MekanismTPforWaystones.MODID)
public class MekanismTPforWaystones {
    public static final String MODID = "mekanism_waystones";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MekanismTPforWaystones(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        modEventBus.addListener(this::commonSetup);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            modEventBus.addListener(this::clientSetup);
            net.kafgchan.mekanism_waystones.client.ModKeyMappings.init(modEventBus);
        });

        // Config 登録
        context.registerConfig(ModConfig.Type.COMMON, TeleporterConfig.SPEC);

        // レジストリ登録
        ModItems.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModMenuTypes.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(net.kafgchan.mekanism_waystones.compat.mekanism.MekanismTeleporterHelper.class);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(ModNetwork::register);
        LOGGER.info("Mekanism: TP for Waystones initialized.");
    }

    private void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> MenuScreens.register(
                ModMenuTypes.PORTABLE_TELEPORTER_PLUS.get(),
                PortableTeleporterPlusScreen::new
        ));
        LOGGER.info("Mekanism: TP for Waystones Client Initialized.");
    }
}
