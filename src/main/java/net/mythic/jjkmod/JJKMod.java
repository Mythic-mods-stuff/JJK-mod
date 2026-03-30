package net.mythic.jjkmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.mythic.jjkmod.command.TestCECommand;
import net.mythic.jjkmod.energy.CursedEnergyManager;
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

		ModNetworking.registerS2CPayloads();

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			TestCECommand.register(dispatcher);
		});

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			CursedEnergyManager.initialize(handler.getPlayer());
			ModNetworking.syncCursedEnergy(handler.getPlayer());
		});

		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			CursedEnergyManager.remove(handler.getPlayer());
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
