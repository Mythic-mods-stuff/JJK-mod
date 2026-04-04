package net.mythic.jjkmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.mythic.jjkmod.character.CharacterSelectionManager;
import net.mythic.jjkmod.character.GradeStatsManager;
import net.mythic.jjkmod.character.JJKCharacter;
import net.mythic.jjkmod.character.JJKGrade;
import net.mythic.jjkmod.command.TestCECommand;
import net.mythic.jjkmod.domain.DomainExpansionManager;
import net.mythic.jjkmod.energy.CursedEnergyManager;
import net.mythic.jjkmod.item.ModItemGroups;
import net.mythic.jjkmod.item.ModItems;
import net.mythic.jjkmod.networking.CharacterSelectedC2SPayload;
import net.mythic.jjkmod.networking.DomainExpansionC2SPayload;
import net.mythic.jjkmod.networking.GradeSelectedC2SPayload;
import net.mythic.jjkmod.networking.ModNetworking;
import net.mythic.jjkmod.sound.ModSounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JJKMod implements ModInitializer {
	public static final String MOD_ID = "jjk-mod";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static int syncTickCounter = 0;

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing JJK Mod - Cursed Energy System");

		// Register items, creative tab, and sounds
		ModItems.initialize();
		ModItemGroups.initialize();
		ModSounds.register();

		// Register network payloads (S2C and C2S)
		ModNetworking.registerS2CPayloads();
		ModNetworking.registerC2SPayloads();

		// Handle character selection — auto-assign Grade 4, apply stats
		ServerPlayNetworking.registerGlobalReceiver(CharacterSelectedC2SPayload.ID, (payload, context) -> {
			var player = context.player();
			player.server.execute(() -> {
				JJKCharacter character = JJKCharacter.fromId(payload.characterId());
				if (character != JJKCharacter.NONE) {
					CharacterSelectionManager.setSelectedCharacter(player, character);

					// Auto-assign Grade 4 if first time with this character
					if (!CharacterSelectionManager.hasGrade(player, character)) {
						CharacterSelectionManager.setGrade(player, character, JJKGrade.GRADE_4);
					}

					// Apply grade stats (CE + HP) for the active character
					JJKGrade grade = CharacterSelectionManager.getGrade(player, character);
					GradeStatsManager.applyGradeStats(player, grade);

					LOGGER.info("Player {} selected character: {} ({})",
							player.getName().getString(),
							character.getDisplayName(),
							grade.getDisplayName());
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
					GradeStatsManager.applyGradeStats(player, grade);
					LOGGER.info("Player {} set grade for {}: {}",
							player.getName().getString(),
							character.getDisplayName(),
							grade.getDisplayName());
				}
			});
		});

		// ── Domain expansion — block-based sphere ─────────────────────
		ServerPlayNetworking.registerGlobalReceiver(DomainExpansionC2SPayload.ID, (payload, context) -> {
			var player = context.player();
			player.server.execute(() -> {
				// Server-side validation: only Gojo can trigger
				JJKCharacter character = CharacterSelectionManager.getSelectedCharacter(player);
				if (character != JJKCharacter.GOJO) {
					return;
				}

				// Activate the block-based domain expansion
				DomainExpansionManager.activateDomain(player);
			});
		});

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			TestCECommand.register(dispatcher);
		});

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			CursedEnergyManager.initialize(handler.getPlayer());

			if (!CharacterSelectionManager.hasSelected(handler.getPlayer())) {
				// New player — open character selection
				ModNetworking.syncCursedEnergy(handler.getPlayer());
				ModNetworking.sendOpenCharacterSelection(handler.getPlayer());
			} else {
				// Returning player — re-apply grade stats
				var player = handler.getPlayer();
				JJKCharacter current = CharacterSelectionManager.getSelectedCharacter(player);

				if (!CharacterSelectionManager.hasGrade(player, current)) {
					CharacterSelectionManager.setGrade(player, current, JJKGrade.GRADE_4);
				}

				JJKGrade grade = CharacterSelectionManager.getGrade(player, current);
				GradeStatsManager.applyGradeStats(player, grade);
			}
		});

		// Re-apply grade stats after respawn (death creates a new player entity,
		// so the temporary health attribute modifier is lost)
		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
			if (CharacterSelectionManager.hasSelected(newPlayer)) {
				JJKCharacter character = CharacterSelectionManager.getSelectedCharacter(newPlayer);
				JJKGrade grade = CharacterSelectionManager.getGrade(newPlayer, character);
				if (grade != null) {
					GradeStatsManager.applyGradeStats(newPlayer, grade);
				}
			}
		});

		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			// Collapse any active domain when the caster disconnects
			DomainExpansionManager.collapseForPlayer(handler.getPlayer().getUuid(), server);

			GradeStatsManager.removeGradeStats(handler.getPlayer());
			CursedEnergyManager.remove(handler.getPlayer());
		});

		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
			// Collapse all domains and restore terrain before shutting down
			DomainExpansionManager.collapseAll(server);

			CharacterSelectionManager.clearAll();
			LOGGER.info("Server stopping - cleared all character selections and grades");
		});

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			// Tick active domain expansions (freeze players, check expiry)
			DomainExpansionManager.tick(server);

			// Cursed energy regeneration
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
