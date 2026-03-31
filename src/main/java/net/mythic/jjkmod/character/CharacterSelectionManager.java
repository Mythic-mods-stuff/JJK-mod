package net.mythic.jjkmod.character;

import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CharacterSelectionManager {

    // Stores the Selected_Character for each player during this world session.
    // Data is only held in memory — cleared when the server stops so the menu
    // re-opens when joining a different world.
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

    /**
     * Clears all stored selections. Called when the server/world stops so that
     * joining a new world forces the character selection menu to open again.
     */
    public static void clearAll() {
        Selected_Character.clear();
    }
}
