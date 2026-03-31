package net.mythic.jjkmod.networking;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record CursedEnergySyncS2CPayload(int currentEnergy, int maxEnergy) implements CustomPayload {

    public static final CustomPayload.Id<CursedEnergySyncS2CPayload> ID =
            new CustomPayload.Id<>(Identifier.of("jjk-mod", "cursed_energy_sync"));

    public static final PacketCodec<RegistryByteBuf, CursedEnergySyncS2CPayload> CODEC =
            PacketCodec.of(
                    (value, buf) -> {
                        buf.writeInt(value.currentEnergy);
                        buf.writeInt(value.maxEnergy);
                    },
                    buf -> new CursedEnergySyncS2CPayload(buf.readInt(), buf.readInt())
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
