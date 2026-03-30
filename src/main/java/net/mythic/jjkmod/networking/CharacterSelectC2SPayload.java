package net.mythic.jjkmod.networking;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Client to Server: Player selects a character
 */
public record CharacterSelectC2SPayload(String character) implements CustomPayload {

    public static final CustomPayload.Id<CharacterSelectC2SPayload> ID =
            new CustomPayload.Id<>(Identifier.of(\"jjk-mod\", \"character_select\"));

    public static final PacketCodec<RegistryByteBuf, CharacterSelectC2SPayload> CODEC =
            PacketCodec.of(
                    (value, buf) -> buf.writeString(value.character),
                    buf -> new CharacterSelectC2SPayload(buf.readString())
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
