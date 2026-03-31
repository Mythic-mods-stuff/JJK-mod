package net.mythic.jjkmod.character;

import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.mythic.jjkmod.JJKMod;
import net.mythic.jjkmod.energy.CursedEnergyManager;
import net.mythic.jjkmod.networking.ModNetworking;

/**
 * Applies grade-based stats (Cursed Energy and Max Health) to players.
 * Called whenever a player's active grade changes — on character selection,
 * character switch, promotion, or reconnect.
 */
public class GradeStatsManager {

    private static final Identifier HEALTH_MODIFIER_ID =
            Identifier.of(JJKMod.MOD_ID, "grade_health_bonus");

    /**
     * Applies the stats for the given grade to the player:
     * <ul>
     *   <li>Sets max Cursed Energy and refills to full</li>
     *   <li>Sets max HP via attribute modifier and heals to full</li>
     * </ul>
     */
    public static void applyGradeStats(ServerPlayerEntity player, JJKGrade grade) {
        if (grade == null) return;

        // ── Cursed Energy ──────────────────────────────────────────────
        CursedEnergyManager.setMaxEnergy(player, grade.getMaxCE());
        CursedEnergyManager.setCurrentEnergy(player, grade.getMaxCE());
        ModNetworking.syncCursedEnergy(player);

        // ── Max Health ─────────────────────────────────────────────────
        EntityAttributeInstance healthAttr =
                player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);

        if (healthAttr != null) {
            // Remove any existing grade modifier
            healthAttr.removeModifier(HEALTH_MODIFIER_ID);

            // Add bonus HP above vanilla 20
            double bonusHP = grade.getMaxHP() - 20.0;
            if (bonusHP > 0) {
                EntityAttributeModifier modifier = new EntityAttributeModifier(
                        HEALTH_MODIFIER_ID,
                        bonusHP,
                        EntityAttributeModifier.Operation.ADD_VALUE
                );
                healthAttr.addTemporaryModifier(modifier);
            }

            // Heal to new max
            player.setHealth(player.getMaxHealth());
        }

        JJKMod.LOGGER.info("Applied grade stats for {}: {} (CE: {}, HP: {})",
                player.getName().getString(),
                grade.getDisplayName(),
                grade.getMaxCE(),
                grade.getMaxHP());
    }

    /**
     * Removes all grade-based stat modifiers from the player.
     * Called on disconnect or server stop.
     */
    public static void removeGradeStats(ServerPlayerEntity player) {
        EntityAttributeInstance healthAttr =
                player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (healthAttr != null) {
            healthAttr.removeModifier(HEALTH_MODIFIER_ID);
        }
    }
}
