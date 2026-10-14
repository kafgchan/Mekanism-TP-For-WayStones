package net.kafgchan.mekanism_waystones.compat.curios;

import net.kafgchan.mekanism_waystones.registration.ModItems;
import net.kafgchan.mekanism_waystones.target.TeleportLocationSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.Map;
import java.util.Optional;

public class CuriosInternalHandler {

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerItem(
                CuriosCapability.ITEM,
                (stack, context) -> new ICurio() {
                    @Override
                    public ItemStack getStack() {
                        return stack;
                    }
                },
                ModItems.PORTABLE_TELEPORTER_PLUS.get()
        );
    }

    @NotNull
    public static ItemStack getItem(Player player, String identifier, int slotIndex) {
        Optional<ICuriosItemHandler> opt = CuriosApi.getCuriosInventory(player);
        if (opt.isPresent()) {
            ICuriosItemHandler handler = opt.get();
            Map<String, ICurioStacksHandler> curios = handler.getCurios();
            ICurioStacksHandler stacksHandler = curios.get(identifier);
            if (stacksHandler != null && slotIndex >= 0 && slotIndex < stacksHandler.getSlots()) {
                return stacksHandler.getStacks().getStackInSlot(slotIndex);
            }
        }
        return ItemStack.EMPTY;
    }

    public static Optional<TeleportLocationSource> findTeleporter(Player player) {
        Optional<ICuriosItemHandler> opt = CuriosApi.getCuriosInventory(player);
        if (opt.isPresent()) {
            ICuriosItemHandler handler = opt.get();
            Optional<SlotResult> result = handler.findFirstCurio(stack -> stack.is(ModItems.PORTABLE_TELEPORTER_PLUS.get()));
            if (result.isPresent()) {
                SlotResult sr = result.get();
                return Optional.of(TeleportLocationSource.ofCurios(sr.slotContext().identifier(), sr.slotContext().index()));
            }
        }
        return Optional.empty();
    }
}
