package net.mythic.jjkmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.mythic.jjkmod.client.ClientCursedEnergyData;
import net.mythic.jjkmod.client.CursedEnergyHudOverlay;
import net.mythic.jjkmod.client.animation.DomainExpansionHandler;
import net.mythic.jjkmod.client.combat.CombatModeHud;
import net.mythic.jjkmod.client.combat.CombatModeManager;
import net.mythic.jjkmod.client.screen.CharacterSelectionScreen;
import net.mythic.jjkmod.client.screen.GradeSelectionScreen;
import net.mythic.jjkmod.networking.CursedEnergySyncS2CPayload;
import net.mythic.jjkmod.networking.OpenCharacterSelectionS2CPayload;
import net.mythic.jjkmod.networking.OpenGradeSelectionS2CPayload;
import org.lwjgl.glfw.GLFW;

public class JJKModClient implements ClientModInitializer {

    private static KeyBinding combatModeKey;
    private static KeyBinding domainExpansionKey;

    @Override
    public void onInitializeClient() {
        // ── Keybindings ────────────────────────────────────────────────
        combatModeKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.jjk-mod.combat_mode",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "category.jjk-mod.keys"
        ));

        domainExpansionKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.jjk-mod.domain_expansion",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_9,
                "category.jjk-mod.keys"
        ));

        // ── Network handlers ───────────────────────────────────────────
        ClientPlayNetworking.registerGlobalReceiver(CursedEnergySyncS2CPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                ClientCursedEnergyData.set(payload.currentEnergy(), payload.maxEnergy());
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(OpenCharacterSelectionS2CPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                context.client().setScreen(new CharacterSelectionScreen());
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(OpenGradeSelectionS2CPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                context.client().setScreen(new GradeSelectionScreen(payload.characterName()));
            });
        });

        // ── HUD rendering ──────────────────────────────────────────────
        HudRenderCallback.EVENT.register(CursedEnergyHudOverlay::render);
        HudRenderCallback.EVENT.register(CombatModeHud::render);

        // ── Key handler + hotbar lock ──────────────────────────────────
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Toggle combat mode on R press (only when not in a screen)
            while (combatModeKey.wasPressed()) {
                if (client.player != null && client.currentScreen == null) {
                    if (!CombatModeManager.isActive()) {
                        // Save current hotbar slot before entering combat mode
                        CombatModeManager.setSavedSlot(client.player.getInventory().selectedSlot);
                    }
                    CombatModeManager.toggle();
                }
            }

            // Domain expansion on 9 press (only in combat mode)
            while (domainExpansionKey.wasPressed()) {
                if (client.player != null && client.currentScreen == null) {
                    DomainExpansionHandler.trigger();
                }
            }

            // Safety net: lock vanilla hotbar slot while in combat mode
            // (Mixins handle the actual input blocking, this catches edge cases)
            if (CombatModeManager.isActive() && client.player != null) {
                client.player.getInventory().selectedSlot = CombatModeManager.getSavedSlot();
            }
        });
    }
}
