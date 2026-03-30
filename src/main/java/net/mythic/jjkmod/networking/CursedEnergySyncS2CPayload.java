package net.mythic.jjkmod.networking;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Combined sync payload for energy AND character data
 */
public record CursedEnergySyncS2CPayload(
        int currentEnergy,
        int maxEnergy,
        String selectedCharacter,
        boolean hasSelectedCharacter
) implements CustomPayload {

    public static final CustomPayload.Id<CursedEnergySyncS2CPayload> ID =
            new CustomPayload.Id<>(Identifier.of(\"jjk-mod\", \"cursed_energy_sync\"));

    public static final PacketCodec<RegistryByteBuf, CursedEnergySyncS2CPayload> CODEC =
            PacketCodec.of(
                    (value, buf) -> {
                        buf.writeInt(value.currentEnergy);
                        buf.writeInt(value.maxEnergy);
                        buf.writeString(value.selectedCharacter);
                        buf.writeBoolean(value.hasSelectedCharacter);
                    },
                    buf -> new CursedEnergySyncS2CPayload(
                            buf.readInt(),
                            buf.readInt(),
                            buf.readString(),
                            buf.readBoolean()
                    )
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
