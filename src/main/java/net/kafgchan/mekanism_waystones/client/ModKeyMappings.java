package net.kafgchan.mekanism_waystones.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.kafgchan.mekanism_waystones.config.TeleporterConfig;
import net.kafgchan.mekanism_waystones.network.PacketOpenTeleporterRequest;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/**
 * テレポート端末呼び出しキーバインドの登録と入力検知 (NeoForge 1.21.1)。
 */
public final class ModKeyMappings {

    public static final String KEY_CATEGORY = "key.categories.mekanism_waystones";
    public static final String KEY_OPEN_TELEPORTER = "key.mekanism_waystones.open_teleporter";

    public static final KeyMapping OPEN_TELEPORTER_KEY = new KeyMapping(
            KEY_OPEN_TELEPORTER,
            KeyConflictContext.IN_GAME,
            KeyModifier.NONE,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            KEY_CATEGORY
    );

    private ModKeyMappings() {
    }

    public static void init(IEventBus modEventBus) {
        modEventBus.addListener(ModKeyMappings::onRegisterKeyMappings);
        NeoForge.EVENT_BUS.addListener(ModKeyMappings::onClientTick);
    }

    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_TELEPORTER_KEY);
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        while (OPEN_TELEPORTER_KEY.consumeClick()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && mc.screen == null && TeleporterConfig.isKeybindEnabled()) {
                PacketDistributor.sendToServer(new PacketOpenTeleporterRequest());
            }
        }
    }
}
