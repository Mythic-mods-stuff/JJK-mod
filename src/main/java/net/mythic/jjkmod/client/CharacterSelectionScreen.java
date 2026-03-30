package net.mythic.jjkmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.mythic.jjkmod.client.CharacterSelectionScreen;
import net.mythic.jjkmod.client.ClientCursedEnergyData;
import net.mythic.jjkmod.client.CursedEnergyHudOverlay;
import net.mythic.jjkmod.networking.CursedEnergySyncS2CPayload;

public class JJKModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Handle sync from server
        ClientPlayNetworking.registerGlobalReceiver(CursedEnergySyncS2CPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                // Update energy
                ClientCursedEnergyData.set(payload.currentEnergy(), payload.maxEnergy());
                // Update character
                ClientCursedEnergyData.setCharacter(payload.selectedCharacter(), payload.hasSelectedCharacter());

                // Show selection screen if no character selected
                if (!payload.hasSelectedCharacter()) {
                    MinecraftClient client = context.client();
                    if (client.currentScreen == null) {
                        client.setScreen(new CharacterSelectionScreen());
                    }
                }
            });
        });

        // Register HUD
        HudRenderCallback.EVENT.register(CursedEnergyHudOverlay::render);
    }
}
