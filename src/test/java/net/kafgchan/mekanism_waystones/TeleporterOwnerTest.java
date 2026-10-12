package net.kafgchan.mekanism_waystones;

import net.kafgchan.mekanism_waystones.item.TeleporterOwner;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TeleporterOwnerTest {

    @Test
    @DisplayName("同一UUIDの場合に所有者として認証される")
    void testOwnerMatches() {
        UUID ownerId = UUID.randomUUID();
        TeleporterOwner owner = new TeleporterOwner(ownerId, "Kaede");

        assertTrue(owner.isOwner(ownerId));
    }

    @Test
    @DisplayName("異なるUUIDまたはnullの場合は所有者として拒否される")
    void testOwnerRejection() {
        UUID ownerId = UUID.randomUUID();
        UUID otherId = UUID.randomUUID();
        TeleporterOwner owner = new TeleporterOwner(ownerId, "Kaede");

        assertFalse(owner.isOwner(otherId));
        assertFalse(owner.isOwner(null));
    }

    @Test
    @DisplayName("NBTの保存および復元が正常に行われる")
    void testNbtSerialization() {
        UUID ownerId = UUID.randomUUID();
        TeleporterOwner owner = new TeleporterOwner(ownerId, "Kaede");

        CompoundTag tag = owner.saveToNbt();
        assertNotNull(tag);

        TeleporterOwner restored = TeleporterOwner.fromNbt(tag);
        assertNotNull(restored);
        assertEquals(owner.uuid(), restored.uuid());
        assertEquals(owner.name(), restored.name());
    }
}
