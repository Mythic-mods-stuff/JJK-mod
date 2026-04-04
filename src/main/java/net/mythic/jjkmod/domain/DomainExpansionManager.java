package net.mythic.jjkmod.domain;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.mythic.jjkmod.JJKMod;

import java.util.*;

/**
 * Server-side manager for block-based domain expansions.
 *
 * <p>When Gojo activates his domain expansion:
 * <ol>
 *   <li>A hollow sphere of black concrete (50-block diameter) is placed around the caster</li>
 *   <li>All existing terrain inside the sphere is cleared to air</li>
 *   <li>A flat floor plane of black concrete is placed at the caster's feet level</li>
 *   <li>All non-caster players inside the sphere are frozen (no movement, mining, or combat)</li>
 *   <li>After 15 seconds the domain collapses and original terrain is restored</li>
 * </ol>
 */
public class DomainExpansionManager {

    /** Sphere radius in blocks (diameter = 50). */
    public static final int RADIUS = 25;

    /** How long the domain lasts (15 seconds × 20 tps = 300 ticks). */
    public static final int DURATION_TICKS = 15 * 20;

    /** Active domains keyed by caster UUID. */
    private static final Map<UUID, ActiveDomain> activeDomains = new LinkedHashMap<>();

    // ── Public API ──────────────────────────────────────────────────

    /**
     * Activate a domain expansion centered on the given caster.
     * Only one domain per caster at a time.
     */
    public static void activateDomain(ServerPlayerEntity caster) {
        if (activeDomains.containsKey(caster.getUuid())) {
            JJKMod.LOGGER.warn("{} already has an active domain",
                    caster.getName().getString());
            return;
        }

        ServerWorld world = caster.getServerWorld();
        BlockPos center = caster.getBlockPos();

        ActiveDomain domain = new ActiveDomain(
                caster.getUuid(), center, world.getRegistryKey());

        // 1. Build sphere shell + floor, clear interior
        buildDomain(world, center, domain);

        // 2. Move caster on top of the floor (floor is at center Y, caster stands on it)
        caster.requestTeleport(caster.getX(), center.getY() + 1.0, caster.getZ());

        // 3. Freeze every non-caster player inside the sphere
        freezePlayersInRange(world, center, caster.getUuid(), domain);

        activeDomains.put(caster.getUuid(), domain);

        JJKMod.LOGGER.info("[Domain] {} activated at {} — {} blocks saved, {} players frozen",
                caster.getName().getString(), center,
                domain.savedBlocks.size(), domain.frozenPlayers.size());
    }

    /**
     * Called every server tick to maintain active domains.
     * Register via {@code ServerTickEvents.END_SERVER_TICK}.
     */
    public static void tick(MinecraftServer server) {
        if (activeDomains.isEmpty()) return;

        Iterator<Map.Entry<UUID, ActiveDomain>> it = activeDomains.entrySet().iterator();
        while (it.hasNext()) {
            ActiveDomain domain = it.next().getValue();
            domain.ticksRemaining--;

            ServerWorld world = server.getWorld(domain.worldKey);
            if (world == null) {
                it.remove();
                continue;
            }

            // Keep frozen players locked in place every tick
            for (Map.Entry<UUID, Vec3d> entry : domain.frozenPlayers.entrySet()) {
                ServerPlayerEntity player = server.getPlayerManager().getPlayer(entry.getKey());
                if (player != null) {
                    Vec3d pos = entry.getValue();
                    player.requestTeleport(pos.x, pos.y, pos.z);
                    player.setVelocity(Vec3d.ZERO);
                    player.velocityModified = true;
                }
            }

            // Catch new players who walk/teleport into the sphere
            Vec3d cv = Vec3d.ofCenter(domain.center);
            for (ServerPlayerEntity player : world.getPlayers()) {
                if (player.getUuid().equals(domain.casterUuid)) continue;
                if (domain.frozenPlayers.containsKey(player.getUuid())) continue;
                if (player.getPos().distanceTo(cv) <= RADIUS) {
                    domain.frozenPlayers.put(player.getUuid(), player.getPos());
                    applyFreezeEffects(player);
                }
            }

            // Domain expired → collapse
            if (domain.ticksRemaining <= 0) {
                collapse(domain, server);
                it.remove();
            }
        }
    }

    /**
     * Force-collapse ALL active domains (e.g. on server shutdown).
     */
    public static void collapseAll(MinecraftServer server) {
        for (ActiveDomain domain : activeDomains.values()) {
            collapse(domain, server);
        }
        activeDomains.clear();
    }

    /**
     * Force-collapse a specific caster's domain (e.g. on disconnect).
     */
    public static void collapseForPlayer(UUID casterUuid, MinecraftServer server) {
        ActiveDomain domain = activeDomains.remove(casterUuid);
        if (domain != null) {
            collapse(domain, server);
        }
    }

    /** Check whether a caster currently has an active domain. */
    public static boolean hasActiveDomain(UUID casterUuid) {
        return activeDomains.containsKey(casterUuid);
    }

    // ── Domain construction ─────────────────────────────────────────

    /**
     * Build the domain sphere: hollow shell of black concrete,
     * interior cleared to air, equator floor plane of black concrete.
     */
    private static void buildDomain(ServerWorld world, BlockPos center, ActiveDomain domain) {
        int r = RADIUS;
        int cx = center.getX(), cy = center.getY(), cz = center.getZ();
        int worldBottom = world.getBottomY();
        int worldTop = world.getTopY() - 1;

        BlockPos.Mutable pos = new BlockPos.Mutable();
        BlockState blackConcrete = Blocks.BLACK_CONCRETE.getDefaultState();
        BlockState air = Blocks.AIR.getDefaultState();

        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                int y = cy + dy;
                if (y < worldBottom || y > worldTop) continue;

                for (int dz = -r; dz <= r; dz++) {
                    double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

                    // Skip positions outside the sphere
                    if (dist > r + 0.5) continue;

                    pos.set(cx + dx, y, cz + dz);
                    BlockState original = world.getBlockState(pos);
                    BlockPos key = pos.toImmutable();

                    // Save original block for restoration later
                    domain.savedBlocks.put(key, original);

                    boolean isShell = dist >= r - 0.5;          // sphere surface
                    boolean isFloor = (dy == 0) && !isShell;    // equator plane (inside shell)

                    if (isShell || isFloor) {
                        // Shell or floor → black concrete
                        if (!original.isOf(Blocks.BLACK_CONCRETE)) {
                            world.setBlockState(key, blackConcrete, Block.NOTIFY_LISTENERS);
                        }
                    } else {
                        // Interior → clear to air
                        if (!original.isAir()) {
                            world.setBlockState(key, air, Block.NOTIFY_LISTENERS);
                        }
                    }
                }
            }
        }
    }

    // ── Player freezing ─────────────────────────────────────────────

    private static void freezePlayersInRange(
            ServerWorld world, BlockPos center, UUID casterUuid, ActiveDomain domain) {
        Vec3d cv = Vec3d.ofCenter(center);
        for (ServerPlayerEntity player : world.getPlayers()) {
            if (player.getUuid().equals(casterUuid)) continue;
            if (player.getPos().distanceTo(cv) <= RADIUS) {
                // Save their position and freeze them
                domain.frozenPlayers.put(player.getUuid(), player.getPos());
                applyFreezeEffects(player);
            }
        }
    }

    private static void applyFreezeEffects(ServerPlayerEntity player) {
        int d = DURATION_TICKS + 40; // slightly longer than domain to cover edge cases
        // Slowness 255  → cannot walk
        player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.SLOWNESS, d, 255, false, false, false));
        // Mining Fatigue 255 → cannot break blocks
        player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.MINING_FATIGUE, d, 255, false, false, false));
        // Weakness 255 → cannot deal damage
        player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.WEAKNESS, d, 255, false, false, false));
    }

    private static void removeFreezeEffects(ServerPlayerEntity player) {
        player.removeStatusEffect(StatusEffects.SLOWNESS);
        player.removeStatusEffect(StatusEffects.MINING_FATIGUE);
        player.removeStatusEffect(StatusEffects.WEAKNESS);
    }

    // ── Domain collapse (restore original terrain) ──────────────────

    private static void collapse(ActiveDomain domain, MinecraftServer server) {
        ServerWorld world = server.getWorld(domain.worldKey);
        if (world != null) {
            // Restore every saved block to its original state
            for (Map.Entry<BlockPos, BlockState> entry : domain.savedBlocks.entrySet()) {
                world.setBlockState(entry.getKey(), entry.getValue(),
                        Block.NOTIFY_LISTENERS);
            }
        }

        // Unfreeze all affected players
        for (UUID uuid : domain.frozenPlayers.keySet()) {
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(uuid);
            if (player != null) {
                removeFreezeEffects(player);
            }
        }

        JJKMod.LOGGER.info("[Domain] Collapsed — {} blocks restored, {} players unfrozen",
                domain.savedBlocks.size(), domain.frozenPlayers.size());
    }

    // ── Domain data holder ──────────────────────────────────────────

    private static class ActiveDomain {
        final UUID casterUuid;
        final BlockPos center;
        final RegistryKey<World> worldKey;
        final Map<BlockPos, BlockState> savedBlocks = new HashMap<>();
        final Map<UUID, Vec3d> frozenPlayers = new HashMap<>();
        int ticksRemaining;

        ActiveDomain(UUID caster, BlockPos center, RegistryKey<World> worldKey) {
            this.casterUuid = caster;
            this.center = center;
            this.worldKey = worldKey;
            this.ticksRemaining = DURATION_TICKS;
        }
    }
}
