package net.mythic.jjkmod.networking;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record OpenCharacterSelectionS2CPayload() implements CustomPayload {

    public static final CustomPayload.Id<OpenCharacterSelectionS2CPayload> ID =
            new CustomPayload.Id<>(Identifier.of("jjk-mod", "open_character_selection"));

    public static final PacketCodec<RegistryByteBuf, OpenCharacterSelectionS2CPayload> CODEC =
            PacketCodec.of(
                    (value, buf) -> { /* nothing to write */ },
                    buf -> new OpenCharacterSelectionS2CPayload()
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
