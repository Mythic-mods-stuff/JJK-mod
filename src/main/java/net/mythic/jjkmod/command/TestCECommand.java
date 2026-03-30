package net.mythic.jjkmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.mythic.jjkmod.energy.CursedEnergyManager;
import net.mythic.jjkmod.networking.ModNetworking;

public class TestCECommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("test")
                        .then(CommandManager.literal("CE")
                                .then(CommandManager.argument("amount", IntegerArgumentType.integer(1))
                                        .then(CommandManager.literal("deplete")
                                                .executes(context -> {
                                                    ServerCommandSource source = context.getSource();
                                                    ServerPlayerEntity player = source.getPlayerOrThrow();
                                                    int amount = IntegerArgumentType.getInteger(context, "amount");

                                                    int before = CursedEnergyManager.getCurrentEnergy(player);
                                                    boolean success = CursedEnergyManager.consume(player, amount);

                                                    if (success) {
                                                        int after = CursedEnergyManager.getCurrentEnergy(player);
                                                        ModNetworking.syncCursedEnergy(player);
                                                        source.sendFeedback(
                                                                () -> Text.literal("Depleted " + amount + " CE (" + before + " -> " + after + ")"),
                                                                false
                                                        );
                                                    } else {
                                                        source.sendError(
                                                                Text.literal("Not enough CE! You have " + before + " but tried to deplete " + amount)
                                                        );
                                                    }

                                                    return success ? 1 : 0;
                                                })
                                        )
                                )
                        )
        );
    }
}
