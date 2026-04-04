package net.mythic.jjkmod.networking;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Client-to-server packet sent when the player triggers domain expansion.
 * The server validates the request and spawns the barrier entity.
 */
public record DomainExpansionC2SPayload() implements CustomPayload {

    public static final CustomPayload.Id<DomainExpansionC2SPayload> ID =
            new CustomPayload.Id<>(Identifier.of("jjk-mod", "domain_expansion"));

    public static final PacketCodec<RegistryByteBuf, DomainExpansionC2SPayload> CODEC =
            PacketCodec.of(
                    (value, buf) -> { /* no fields to write */ },
                    buf -> new DomainExpansionC2SPayload()
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
