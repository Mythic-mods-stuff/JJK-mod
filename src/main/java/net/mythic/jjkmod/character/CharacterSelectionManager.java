package net.mythic.jjkmod.character;

import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CharacterSelectionManager {

    // Stores the Selected_Character for each player during this world session.
    // Data is only held in memory — when the server stops (or the world closes),
    // all selections are cleared, so players must pick again in a new world.
    private static final Map<UUID, JJKCharacter> Selected_Character = new HashMap<>();

    public static boolean hasSelected(ServerPlayerEntity player) {
        JJKCharacter selection = Selected_Character.get(player.getUuid());
        return selection != null && selection != JJKCharacter.NONE;
    }

    public static JJKCharacter getSelectedCharacter(ServerPlayerEntity player) {
        return Selected_Character.getOrDefault(player.getUuid(), JJKCharacter.NONE);
    }

    public static void setSelectedCharacter(ServerPlayerEntity player, JJKCharacter character) {
        Selected_Character.put(player.getUuid(), character);
    }

    public static void remove(ServerPlayerEntity player) {
        Selected_Character.remove(player.getUuid());
    }
}
