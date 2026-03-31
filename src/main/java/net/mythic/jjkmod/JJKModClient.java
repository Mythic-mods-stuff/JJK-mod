package net.mythic.jjkmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.mythic.jjkmod.client.ClientCursedEnergyData;
import net.mythic.jjkmod.client.CursedEnergyHudOverlay;
import net.mythic.jjkmod.client.screen.CharacterSelectionScreen;
import net.mythic.jjkmod.networking.CursedEnergySyncS2CPayload;
import net.mythic.jjkmod.networking.OpenCharacterSelectionS2CPayload;

public class JJKModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Sync cursed energy data from server
        ClientPlayNetworking.registerGlobalReceiver(CursedEnergySyncS2CPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                ClientCursedEnergyData.set(payload.currentEnergy(), payload.maxEnergy());
            });
        });

        // Open character selection screen when the server requests it
        ClientPlayNetworking.registerGlobalReceiver(OpenCharacterSelectionS2CPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                context.client().setScreen(new CharacterSelectionScreen());
            });
        });

        HudRenderCallback.EVENT.register(CursedEnergyHudOverlay::render);
    }
}
