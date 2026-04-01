package net.mythic.jjkmod.character;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.mythic.jjkmod.JJKMod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Persists player grades to the world save. Grades survive server restarts.
 * Stored as NBT in the world's {@code data/jjk_mod_grades.dat} file.
 *
 * <p>Structure: {@code { grades: { "<uuid>": { "<characterId>": "<gradeId>" } } }}
 */
public class GradeSaveData extends PersistentState {

    private final Map<UUID, Map<String, String>> playerGrades = new HashMap<>();

    public GradeSaveData() {
    }

    // ── Read / Write ───────────────────────────────────────────────────

    public void setGrade(UUID playerId, String characterId, String gradeId) {
        playerGrades.computeIfAbsent(playerId, k -> new HashMap<>()).put(characterId, gradeId);
        markDirty();
    }

    public String getGrade(UUID playerId, String characterId) {
        Map<String, String> chars = playerGrades.get(playerId);
        return chars != null ? chars.get(characterId) : null;
    }

    public boolean hasGrade(UUID playerId, String characterId) {
        Map<String, String> chars = playerGrades.get(playerId);
        return chars != null && chars.containsKey(characterId);
    }

    // ── Serialization ──────────────────────────────────────────────────

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        NbtCompound gradesNbt = new NbtCompound();
        for (Map.Entry<UUID, Map<String, String>> entry : playerGrades.entrySet()) {
            NbtCompound playerNbt = new NbtCompound();
            for (Map.Entry<String, String> charEntry : entry.getValue().entrySet()) {
                playerNbt.putString(charEntry.getKey(), charEntry.getValue());
            }
            gradesNbt.put(entry.getKey().toString(), playerNbt);
        }
        nbt.put("grades", gradesNbt);
        return nbt;
    }

    public static GradeSaveData fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        GradeSaveData data = new GradeSaveData();
        NbtCompound gradesNbt = nbt.getCompound("grades");
        for (String uuidStr : gradesNbt.getKeys()) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                NbtCompound playerNbt = gradesNbt.getCompound(uuidStr);
                Map<String, String> charGrades = new HashMap<>();
                for (String charId : playerNbt.getKeys()) {
                    charGrades.put(charId, playerNbt.getString(charId));
                }
                data.playerGrades.put(uuid, charGrades);
            } catch (IllegalArgumentException e) {
                JJKMod.LOGGER.warn("Invalid UUID in grade save data: {}", uuidStr);
            }
        }
        return data;
    }

    // ── Access ──────────────────────────────────────────────────────────

    private static final Type<GradeSaveData> TYPE = new Type<>(
            GradeSaveData::new,
            GradeSaveData::fromNbt,
            null
    );

    /**
     * Gets the grade save data for the current world.
     * Creates a new instance if none exists yet.
     */
    public static GradeSaveData get(MinecraftServer server) {
        PersistentStateManager manager = server.getOverworld().getPersistentStateManager();
        return manager.getOrCreate(TYPE, "jjk_mod_grades");
    }
}
