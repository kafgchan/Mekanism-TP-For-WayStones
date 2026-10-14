package net.kafgchan.mekanism_waystones.registration;

import net.kafgchan.mekanism_waystones.MekanismTPforWaystones;
import net.kafgchan.mekanism_waystones.menu.PortableTeleporterPlusMenu;
import net.kafgchan.mekanism_waystones.target.TeleportTarget;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.Supplier;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, MekanismTPforWaystones.MODID);

    public static final Supplier<MenuType<PortableTeleporterPlusMenu>> PORTABLE_TELEPORTER_PLUS =
            MENUS.register("portable_teleporter_plus", () -> IMenuTypeExtension.create((containerId, inv, buf) -> {
                net.kafgchan.mekanism_waystones.target.TeleportLocationSource source =
                        net.kafgchan.mekanism_waystones.target.TeleportLocationSource.STREAM_CODEC.decode(buf);
                int energy = buf.readVarInt();
                List<TeleportTarget> targets = TeleportTarget.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buf);
                return new PortableTeleporterPlusMenu(containerId, inv, source, targets, energy);
            }));

    public static void register(IEventBus modEventBus) {
        MENUS.register(modEventBus);
    }
}
