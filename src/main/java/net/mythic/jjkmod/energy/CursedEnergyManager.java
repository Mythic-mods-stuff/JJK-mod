package net.mythic.jjkmod.energy;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import net.minecraft.registry.RegistryWrapper;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CursedEnergyManager extends PersistentState {
    public static final int DEFAULT_MAX_ENERGY = 500;
    public static final int DEFAULT_ENERGY = 500;
    public static final int DEFAULT_CURSED_ENERGY_REGENERATION_RATE = 1;

    // Player data: [currentEnergy, maxEnergy]
    private final Map<UUID, int[]> playerEnergy = new HashMap<>();
    // Character selection storage
    private final Map<UUID, String> playerCharacters = new HashMap<>();

    public CursedEnergyManager() {
        super();
    }

    // ============ PERSISTENT STATE METHODS ============

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound charactersNbt = new NbtCompound();
        for (Map.Entry<UUID, String> entry : playerCharacters.entrySet()) {
            charactersNbt.putString(entry.getKey().toString(), entry.getValue());
        }
        nbt.put(\"characters\", charactersNbt);
        return nbt;
    }

    public static CursedEnergyManager createFromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        CursedEnergyManager manager = new CursedEnergyManager();
        NbtCompound charactersNbt = nbt.getCompound(\"characters\");
        for (String key : charactersNbt.getKeys()) {
            manager.playerCharacters.put(UUID.fromString(key), charactersNbt.getString(key));
        }
        return manager;
    }

    private static Type<CursedEnergyManager> type = new Type<>(
            CursedEnergyManager::new,
            CursedEnergyManager::createFromNbt,
            null
    );

    public static CursedEnergyManager getServerState(MinecraftServer server) {
        PersistentStateManager manager = server.getWorld(World.OVERWORLD).getPersistentStateManager();
        CursedEnergyManager state = manager.getOrCreate(type, \"jjkmod_data\");
                state.markDirty();
        return state;
    }

    // ============ ENERGY METHODS (now instance-based for persistence) ============

    private static final Map<UUID, int[]> PLAYER_ENERGY = new HashMap<>();

    public static void initialize(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        if (!PLAYER_ENERGY.containsKey(uuid)) {
            PLAYER_ENERGY.put(uuid, new int[]{DEFAULT_ENERGY, DEFAULT_MAX_ENERGY});
        }
    }

    public static int getCurrentEnergy(ServerPlayerEntity player) {
        int[] data = PLAYER_ENERGY.get(player.getUuid());
        return data != null ? data[0] : DEFAULT_ENERGY;
    }

    public static int getMaxEnergy(ServerPlayerEntity player) {
        int[] data = PLAYER_ENERGY.get(player.getUuid());
        return data != null ? data[1] : DEFAULT_MAX_ENERGY;
    }

    public static void setCurrentEnergy(ServerPlayerEntity player, int value) {
        int[] data = PLAYER_ENERGY.computeIfAbsent(player.getUuid(),
                k -> new int[]{DEFAULT_ENERGY, DEFAULT_MAX_ENERGY});
        data[0] = Math.max(0, Math.min(value, data[1]));
    }

    public static void setMaxEnergy(ServerPlayerEntity player, int value) {
        int[] data = PLAYER_ENERGY.computeIfAbsent(player.getUuid(),
                k -> new int[]{DEFAULT_ENERGY, DEFAULT_MAX_ENERGY});
        data[1] = Math.max(0, value);
        if (data[0] > data[1]) {
            data[0] = data[1];
        }
    }

    public static boolean consume(ServerPlayerEntity player, int amount) {
        int current = getCurrentEnergy(player);
        if (current >= amount) {
            setCurrentEnergy(player, current - amount);
            return true;
        }
        return false;
    }

    public static void regenerate(ServerPlayerEntity player, int amount) {
        int current = getCurrentEnergy(player);
        int max = getMaxEnergy(player);
        setCurrentEnergy(player, Math.min(current + amount, max));
    }

    public static void remove(ServerPlayerEntity player) {
        PLAYER_ENERGY.remove(player.getUuid());
    }

    // ============ CHARACTER METHODS ============

    public static boolean hasSelectedCharacter(ServerPlayerEntity player) {
        CursedEnergyManager state = getServerState(player.getServer());
        return state.playerCharacters.containsKey(player.getUuid());
    }

    public static String getSelectedCharacter(ServerPlayerEntity player) {
        CursedEnergyManager state = getServerState(player.getServer());
        return state.playerCharacters.get(player.getUuid());
    }

    public static void setSelectedCharacter(ServerPlayerEntity player, String character) {
        CursedEnergyManager state = getServerState(player.getServer());
        state.playerCharacters.put(player.getUuid(), character);
        state.markDirty();
    }
}