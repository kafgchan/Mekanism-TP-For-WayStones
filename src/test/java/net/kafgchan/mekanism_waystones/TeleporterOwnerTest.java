package net.kafgchan.mekanism_waystones;

import net.kafgchan.mekanism_waystones.item.TeleporterOwner;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
}
