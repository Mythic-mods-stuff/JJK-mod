package net.mythic.jjkmod.networking;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record GradeSelectedC2SPayload(String gradeId) implements CustomPayload {

    public static final CustomPayload.Id<GradeSelectedC2SPayload> ID =
            new CustomPayload.Id<>(Identifier.of("jjk-mod", "grade_selected"));

    public static final PacketCodec<RegistryByteBuf, GradeSelectedC2SPayload> CODEC =
            PacketCodec.of(
                    (value, buf) -> buf.writeString(value.gradeId),
                    buf -> new GradeSelectedC2SPayload(buf.readString())
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
