package net.mythic.jjkmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.mythic.jjkmod.character.CharacterSelectionManager;
import net.mythic.jjkmod.character.JJKCharacter;
import net.mythic.jjkmod.command.TestCECommand;
import net.mythic.jjkmod.energy.CursedEnergyManager;
import net.mythic.jjkmod.networking.CharacterSelectedC2SPayload;
import net.mythic.jjkmod.networking.ModNetworking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JJKMod implements ModInitializer {
	public static final String MOD_ID = "jjk-mod";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static int syncTickCounter = 0;

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing JJK Mod - Cursed Energy System");

		// Register network payloads (S2C and C2S)
		ModNetworking.registerS2CPayloads();
		ModNetworking.registerC2SPayloads();

		// Handle character selection packets from clients
		ServerPlayNetworking.registerGlobalReceiver(CharacterSelectedC2SPayload.ID, (payload, context) -> {
			var player = context.player();
			player.server.execute(() -> {
				JJKCharacter character = JJKCharacter.fromId(payload.characterId());
				if (character != JJKCharacter.NONE) {
					CharacterSelectionManager.setSelectedCharacter(player, character);
					LOGGER.info("Player {} selected character: {}",
							player.getName().getString(), character.getDisplayName());
				}
			});
		});

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			TestCECommand.register(dispatcher);
		});

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			CursedEnergyManager.initialize(handler.getPlayer());
			ModNetworking.syncCursedEnergy(handler.getPlayer());

			// If the player hasn't selected a character yet in this world, open the menu
			if (!CharacterSelectionManager.hasSelected(handler.getPlayer())) {
				ModNetworking.sendOpenCharacterSelection(handler.getPlayer());
			}
		});

		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			CursedEnergyManager.remove(handler.getPlayer());
			// Note: CharacterSelectionManager data is intentionally kept so
			// reconnecting players keep their selection within the same world.
		});

		// When the server/world stops, wipe all character selections so the menu
		// re-opens the next time a player joins a (new) world.
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
			CharacterSelectionManager.clearAll();
			LOGGER.info("Server stopping - cleared all character selections");
		});

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			syncTickCounter++;
			if (syncTickCounter >= CursedEnergyManager.DEFAULT_CURSED_ENERGY_REGENERATION_RATE) {
				syncTickCounter = 0;
				for (var player : server.getPlayerManager().getPlayerList()) {
					CursedEnergyManager.regenerate(player, 1);
					ModNetworking.syncCursedEnergy(player);
				}
			}
		});
	}
}
