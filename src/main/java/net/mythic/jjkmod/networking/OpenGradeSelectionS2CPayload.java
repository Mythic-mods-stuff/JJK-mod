package net.mythic.jjkmod.networking;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record OpenGradeSelectionS2CPayload(String characterName) implements CustomPayload {

    public static final CustomPayload.Id<OpenGradeSelectionS2CPayload> ID =
            new CustomPayload.Id<>(Identifier.of("jjk-mod", "open_grade_selection"));

    public static final PacketCodec<RegistryByteBuf, OpenGradeSelectionS2CPayload> CODEC =
            PacketCodec.of(
                    (value, buf) -> buf.writeString(value.characterName),
                    buf -> new OpenGradeSelectionS2CPayload(buf.readString())
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
