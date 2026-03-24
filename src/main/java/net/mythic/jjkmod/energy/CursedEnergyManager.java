package net.mythic.jjkmod.energy;

import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CursedEnergyManager {

    public static final int DEFAULT_MAX_ENERGY = 500;
    public static final int DEFAULT_ENERGY = 500;
    public static final int DEFAULT_CURSED_ENERGY_REGENERATION_RATE = 20;

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
}
