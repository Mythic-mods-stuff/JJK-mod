package net.mythic.jjkmod.networking;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.mythic.jjkmod.energy.CursedEnergyManager;

public class ModNetworking {

    public static void registerPayloads() {
        // Server to Client
        PayloadTypeRegistry.playS2C().register(CursedEnergySyncS2CPayload.ID, CursedEnergySyncS2CPayload.CODEC);
        // Client to Server
        PayloadTypeRegistry.playC2S().register(CharacterSelectC2SPayload.ID, CharacterSelectC2SPayload.CODEC);
    }

    public static void registerServerReceivers() {
        // Handle character selection from client
        ServerPlayNetworking.registerGlobalReceiver(CharacterSelectC2SPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            String character = payload.character();

            context.server().execute(() -> {
                CursedEnergyManager.setSelectedCharacter(player, character);
                syncToClient(player);
            });
        });
    }

    /**
     * Sync all player data (energy + character) to client
     */
    public static void syncToClient(ServerPlayerEntity player) {
        int current = CursedEnergyManager.getCurrentEnergy(player);
        int max = CursedEnergyManager.getMaxEnergy(player);
        boolean hasChar = CursedEnergyManager.hasSelectedCharacter(player);
        String character = hasChar ? CursedEnergyManager.getSelectedCharacter(player) : \"\";

        ServerPlayNetworking.send(player, new CursedEnergySyncS2CPayload(current, max, character, hasChar));
    }

    /**
     * Send character selection to server (called from client)
     */
    public static void sendCharacterSelection(String character) {
        ClientPlayNetworking.send(new CharacterSelectC2SPayload(character));
    }
}