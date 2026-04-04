package net.mythic.jjkmod.networking;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.mythic.jjkmod.energy.CursedEnergyManager;

public class ModNetworking {

    public static void registerS2CPayloads() {
        PayloadTypeRegistry.playS2C().register(CursedEnergySyncS2CPayload.ID, CursedEnergySyncS2CPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(OpenCharacterSelectionS2CPayload.ID, OpenCharacterSelectionS2CPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(OpenGradeSelectionS2CPayload.ID, OpenGradeSelectionS2CPayload.CODEC);
    }

    public static void registerC2SPayloads() {
        PayloadTypeRegistry.playC2S().register(CharacterSelectedC2SPayload.ID, CharacterSelectedC2SPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(GradeSelectedC2SPayload.ID, GradeSelectedC2SPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(DomainExpansionC2SPayload.ID, DomainExpansionC2SPayload.CODEC);
    }

    public static void syncCursedEnergy(ServerPlayerEntity player) {
        int current = CursedEnergyManager.getCurrentEnergy(player);
        int max = CursedEnergyManager.getMaxEnergy(player);
        ServerPlayNetworking.send(player, new CursedEnergySyncS2CPayload(current, max));
    }

    public static void sendOpenCharacterSelection(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, new OpenCharacterSelectionS2CPayload());
    }

    public static void sendOpenGradeSelection(ServerPlayerEntity player, String characterName) {
        ServerPlayNetworking.send(player, new OpenGradeSelectionS2CPayload(characterName));
    }
}
