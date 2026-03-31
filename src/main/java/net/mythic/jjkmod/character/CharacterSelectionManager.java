package net.mythic.jjkmod.character;

import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CharacterSelectionManager {

    // Stores the Selected_Character for each player during this world session.
    private static final Map<UUID, JJKCharacter> Selected_Character = new HashMap<>();

    // Stores per-character grades for each player. Grades are bound to the
    // character, not the player — switching characters preserves each grade.
    private static final Map<UUID, Map<JJKCharacter, JJKGrade>> characterGrades = new HashMap<>();

    // ── Character selection ─────────────────────────────────────────────

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

    // ── Grade management ────────────────────────────────────────────────

    public static boolean hasGrade(ServerPlayerEntity player, JJKCharacter character) {
        Map<JJKCharacter, JJKGrade> grades = characterGrades.get(player.getUuid());
        return grades != null && grades.containsKey(character);
    }

    public static JJKGrade getGrade(ServerPlayerEntity player, JJKCharacter character) {
        Map<JJKCharacter, JJKGrade> grades = characterGrades.get(player.getUuid());
        if (grades == null) return null;
        return grades.get(character);
    }

    public static void setGrade(ServerPlayerEntity player, JJKCharacter character, JJKGrade grade) {
        characterGrades.computeIfAbsent(player.getUuid(), k -> new HashMap<>()).put(character, grade);
    }

    /**
     * Returns the grade for the player's currently active character, or null.
     */
    public static JJKGrade getActiveGrade(ServerPlayerEntity player) {
        JJKCharacter character = getSelectedCharacter(player);
        if (character == JJKCharacter.NONE) return null;
        return getGrade(player, character);
    }

    // ── Lifecycle ───────────────────────────────────────────────────────

    /**
     * Clears all stored selections and grades. Called when the server/world
     * stops so that joining a new world forces fresh selection.
     */
    public static void clearAll() {
        Selected_Character.clear();
        characterGrades.clear();
    }
}
