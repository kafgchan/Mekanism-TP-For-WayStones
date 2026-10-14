package net.kafgchan.mekanism_waystones.network;

import net.kafgchan.mekanism_waystones.MekanismTPforWaystones;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class ModPayloads {

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(MekanismTPforWaystones.MODID).versioned("1.0.0");

        registrar.playToServer(
                PacketTeleportRequest.TYPE,
                PacketTeleportRequest.STREAM_CODEC,
                PacketTeleportRequest::handle
        );

        registrar.playToServer(
                PacketOpenTeleporterRequest.TYPE,
                PacketOpenTeleporterRequest.STREAM_CODEC,
                PacketOpenTeleporterRequest::handle
        );
    }
}
