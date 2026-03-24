package net.mythic.jjkmod.client;

public class ClientCursedEnergyData {

    private static int currentEnergy = 0;
    private static int maxEnergy = 500;

    public static int getCurrentEnergy() {
        return currentEnergy;
    }

    public static int getMaxEnergy() {
        return maxEnergy;
    }

    public static void set(int current, int max) {
        currentEnergy = current;
        maxEnergy = max;
    }
}
