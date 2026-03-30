package net.mythic.jjkmod.networking;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.mythic.jjkmod.energy.CursedEnergyManager;

public class ModNetworking {

    public static void registerS2CPayloads() {
        PayloadTypeRegistry.playS2C().register(CursedEnergySyncS2CPayload.ID, CursedEnergySyncS2CPayload.CODEC);
    }

    public static void syncCursedEnergy(ServerPlayerEntity player) {
        int current = CursedEnergyManager.getCurrentEnergy(player);
        int max = CursedEnergyManager.getMaxEnergy(player);
        ServerPlayNetworking.send(player, new CursedEnergySyncS2CPayload(current, max));
    }
}
