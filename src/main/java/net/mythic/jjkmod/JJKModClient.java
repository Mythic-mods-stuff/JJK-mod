package net.mythic.jjkmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.mythic.jjkmod.client.ClientCursedEnergyData;
import net.mythic.jjkmod.client.CursedEnergyHudOverlay;
import net.mythic.jjkmod.networking.CursedEnergySyncS2CPayload;

public class JJKModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(CursedEnergySyncS2CPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                ClientCursedEnergyData.set(payload.currentEnergy(), payload.maxEnergy());
            });
        });

        HudRenderCallback.EVENT.register(CursedEnergyHudOverlay::render);
    }
}
