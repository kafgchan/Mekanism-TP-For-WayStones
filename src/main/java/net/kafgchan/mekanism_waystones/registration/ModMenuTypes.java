package net.kafgchan.mekanism_waystones.registration;

import net.kafgchan.mekanism_waystones.MekanismTPforWaystones;
import net.kafgchan.mekanism_waystones.menu.PortableTeleporterPlusMenu;
import net.kafgchan.mekanism_waystones.target.TeleportLocationSource;
import net.kafgchan.mekanism_waystones.target.TeleportTarget;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;

public final class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, MekanismTPforWaystones.MODID);

    public static final RegistryObject<MenuType<PortableTeleporterPlusMenu>> PORTABLE_TELEPORTER_PLUS =
            MENUS.register("portable_teleporter_plus", () -> IForgeMenuType.create((containerId, inv, buf) -> {
                TeleportLocationSource source = TeleportLocationSource.decode(buf);
                int energy = buf.readVarInt();
                int size = buf.readVarInt();
                List<TeleportTarget> targets = new ArrayList<>(size);
                for (int i = 0; i < size; i++) {
                    targets.add(TeleportTarget.decode(buf));
                }
                return new PortableTeleporterPlusMenu(containerId, inv, source, targets, energy);
            }));

    private ModMenuTypes() {
    }

    public static void register(IEventBus modEventBus) {
        MENUS.register(modEventBus);
    }
}
