package net.mythic.jjkmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.mythic.jjkmod.character.CharacterSelectionManager;
import net.mythic.jjkmod.character.JJKCharacter;
import net.mythic.jjkmod.character.JJKGrade;
import net.mythic.jjkmod.command.TestCECommand;
import net.mythic.jjkmod.energy.CursedEnergyManager;
import net.mythic.jjkmod.item.ModItemGroups;
import net.mythic.jjkmod.item.ModItems;
import net.mythic.jjkmod.networking.CharacterSelectedC2SPayload;
import net.mythic.jjkmod.networking.GradeSelectedC2SPayload;
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

		// Register items and creative tab
		ModItems.initialize();
		ModItemGroups.initialize();

		// Register network payloads (S2C and C2S)
		ModNetworking.registerS2CPayloads();
		ModNetworking.registerC2SPayloads();

		// Handle character selection — auto-assign Grade 4 when first picking
		ServerPlayNetworking.registerGlobalReceiver(CharacterSelectedC2SPayload.ID, (payload, context) -> {
			var player = context.player();
			player.server.execute(() -> {
				JJKCharacter character = JJKCharacter.fromId(payload.characterId());
				if (character != JJKCharacter.NONE) {
					CharacterSelectionManager.setSelectedCharacter(player, character);

					// Auto-assign Grade 4 if this is the first time with this character
					if (!CharacterSelectionManager.hasGrade(player, character)) {
						CharacterSelectionManager.setGrade(player, character, JJKGrade.GRADE_4);
						LOGGER.info("Player {} selected character: {} (assigned Grade 4)",
								player.getName().getString(), character.getDisplayName());
					} else {
						LOGGER.info("Player {} switched to character: {} (Grade: {})",
								player.getName().getString(),
								character.getDisplayName(),
								CharacterSelectionManager.getGrade(player, character).getDisplayName());
					}
				}
			});
		});

		// Keep the grade C2S handler registered (for potential future use)
		ServerPlayNetworking.registerGlobalReceiver(GradeSelectedC2SPayload.ID, (payload, context) -> {
			var player = context.player();
			player.server.execute(() -> {
				JJKCharacter character = CharacterSelectionManager.getSelectedCharacter(player);
				JJKGrade grade = JJKGrade.fromId(payload.gradeId());
				if (character != JJKCharacter.NONE && grade != null) {
					CharacterSelectionManager.setGrade(player, character, grade);
					LOGGER.info("Player {} set grade for {}: {}",
							player.getName().getString(),
							character.getDisplayName(),
							grade.getDisplayName());
				}
			});
		});

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			TestCECommand.register(dispatcher);
		});

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			CursedEnergyManager.initialize(handler.getPlayer());
			ModNetworking.syncCursedEnergy(handler.getPlayer());

			if (!CharacterSelectionManager.hasSelected(handler.getPlayer())) {
				ModNetworking.sendOpenCharacterSelection(handler.getPlayer());
			} else {
				// Player reconnected — ensure they have a grade
				JJKCharacter current = CharacterSelectionManager.getSelectedCharacter(handler.getPlayer());
				if (!CharacterSelectionManager.hasGrade(handler.getPlayer(), current)) {
					CharacterSelectionManager.setGrade(handler.getPlayer(), current, JJKGrade.GRADE_4);
				}
			}
		});

		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			CursedEnergyManager.remove(handler.getPlayer());
		});

		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
			CharacterSelectionManager.clearAll();
			LOGGER.info("Server stopping - cleared all character selections and grades");
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
