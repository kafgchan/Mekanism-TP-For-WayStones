package net.kafgchan.mekanism_waystones;

import io.netty.buffer.Unpooled;
import net.kafgchan.mekanism_waystones.target.TeleportLocationSource;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TeleportLocationSourceTest {

    @Test
    @DisplayName("Hand source factory and methods")
    void testHandSource() {
        TeleportLocationSource mainHand = TeleportLocationSource.ofHand(InteractionHand.MAIN_HAND);
        assertEquals(TeleportLocationSource.SourceType.HAND, mainHand.getType());
        assertEquals(0, mainHand.getSlotIndex());
        assertEquals(InteractionHand.MAIN_HAND, mainHand.getHand());

        TeleportLocationSource offHand = TeleportLocationSource.ofHand(InteractionHand.OFF_HAND);
        assertEquals(TeleportLocationSource.SourceType.HAND, offHand.getType());
        assertEquals(1, offHand.getSlotIndex());
        assertEquals(InteractionHand.OFF_HAND, offHand.getHand());
    }

    @Test
    @DisplayName("Curios source factory and methods")
    void testCuriosSource() {
        TeleportLocationSource curios = TeleportLocationSource.ofCurios("charm", 2);
        assertEquals(TeleportLocationSource.SourceType.CURIOS, curios.getType());
        assertEquals(2, curios.getSlotIndex());
        assertEquals("charm", curios.getCuriosIdentifier());
    }

    @Test
    @DisplayName("Inventory source factory and methods")
    void testInventorySource() {
        TeleportLocationSource inv = TeleportLocationSource.ofInventory(8);
        assertEquals(TeleportLocationSource.SourceType.INVENTORY, inv.getType());
        assertEquals(8, inv.getSlotIndex());
    }

    @Test
    @DisplayName("Encode and decode roundtrip")
    void testEncodeDecode() {
        TeleportLocationSource source = TeleportLocationSource.ofCurios("curio", 1);
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

        source.encode(buf);
        TeleportLocationSource decoded = TeleportLocationSource.decode(buf);

        assertEquals(source.getType(), decoded.getType());
        assertEquals(source.getSlotIndex(), decoded.getSlotIndex());
        assertEquals(source.getCuriosIdentifier(), decoded.getCuriosIdentifier());
    }
}
