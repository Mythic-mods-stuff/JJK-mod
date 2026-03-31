package net.mythic.jjkmod.networking;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record CharacterSelectedC2SPayload(String characterId) implements CustomPayload {

    public static final CustomPayload.Id<CharacterSelectedC2SPayload> ID =
            new CustomPayload.Id<>(Identifier.of("jjk-mod", "character_selected"));

    public static final PacketCodec<RegistryByteBuf, CharacterSelectedC2SPayload> CODEC =
            PacketCodec.of(
                    (value, buf) -> buf.writeString(value.characterId),
                    buf -> new CharacterSelectedC2SPayload(buf.readString())
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
