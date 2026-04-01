package net.mythic.jjkmod.character;

import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CharacterSelectionManager {

    // Character selection — per-session only (pick each time you join)
    private static final Map<UUID, JJKCharacter> Selected_Character = new HashMap<>();

    // In-memory grade cache (fast lookups). Backed by GradeSaveData for persistence.
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

    // ── Grade management (persistent) ───────────────────────────────────

    public static boolean hasGrade(ServerPlayerEntity player, JJKCharacter character) {
        // Check in-memory cache
        Map<JJKCharacter, JJKGrade> grades = characterGrades.get(player.getUuid());
        if (grades != null && grades.containsKey(character)) return true;

        // Check persistent storage
        if (player.server != null) {
            GradeSaveData saveData = GradeSaveData.get(player.server);
            return saveData.hasGrade(player.getUuid(), character.getId());
        }
        return false;
    }

    public static JJKGrade getGrade(ServerPlayerEntity player, JJKCharacter character) {
        // Check in-memory cache first
        Map<JJKCharacter, JJKGrade> grades = characterGrades.get(player.getUuid());
        if (grades != null && grades.containsKey(character)) {
            return grades.get(character);
        }

        // Load from persistent storage
        if (player.server != null) {
            GradeSaveData saveData = GradeSaveData.get(player.server);
            String gradeId = saveData.getGrade(player.getUuid(), character.getId());
            if (gradeId != null) {
                JJKGrade grade = JJKGrade.fromId(gradeId);
                if (grade != null) {
                    // Populate in-memory cache
                    characterGrades.computeIfAbsent(player.getUuid(), k -> new HashMap<>())
                            .put(character, grade);
                    return grade;
                }
            }
        }

        return null;
    }

    public static void setGrade(ServerPlayerEntity player, JJKCharacter character, JJKGrade grade) {
        // Update in-memory cache
        characterGrades.computeIfAbsent(player.getUuid(), k -> new HashMap<>())
                .put(character, grade);

        // Persist to world save
        if (player.server != null) {
            GradeSaveData saveData = GradeSaveData.get(player.server);
            saveData.setGrade(player.getUuid(), character.getId(), grade.getId());
        }
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
     * Clears in-memory session data. Grades are NOT cleared because they
     * are persisted via {@link GradeSaveData}. Called on server stop.
     */
    public static void clearAll() {
        Selected_Character.clear();
        characterGrades.clear();
    }
}
