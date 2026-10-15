package net.kafgchan.mekanism_waystones.network;

import net.kafgchan.mekanism_waystones.MekanismTPforWaystones;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModNetwork {
    private static final String PROTOCOL_VERSION = "1.0.0";
    @SuppressWarnings("removal")
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MekanismTPforWaystones.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    private static int id() {
        return packetId++;
    }

    private ModNetwork() {
    }

    public static void register() {
        CHANNEL.messageBuilder(PacketTeleportRequest.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(PacketTeleportRequest::decode)
                .encoder(PacketTeleportRequest::encode)
                .consumerNetworkThread(PacketTeleportRequest::handle)
                .add();

        CHANNEL.messageBuilder(PacketOpenTeleporterRequest.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(PacketOpenTeleporterRequest::decode)
                .encoder(PacketOpenTeleporterRequest::encode)
                .consumerNetworkThread(PacketOpenTeleporterRequest::handle)
                .add();

        CHANNEL.messageBuilder(PacketToggleFavorite.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(PacketToggleFavorite::decode)
                .encoder(PacketToggleFavorite::encode)
                .consumerNetworkThread(PacketToggleFavorite::handle)
                .add();

        CHANNEL.messageBuilder(PacketSetQuickTarget.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(PacketSetQuickTarget::decode)
                .encoder(PacketSetQuickTarget::encode)
                .consumerNetworkThread(PacketSetQuickTarget::handle)
                .add();
    }


    public static void sendToServer(Object msg) {
        CHANNEL.sendToServer(msg);
    }
}
