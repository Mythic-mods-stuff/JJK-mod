package net.mythic.jjkmod.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * A purely visual entity representing the domain expansion barrier.
 *
 * <p>Spawned server-side when a player activates domain expansion.
 * Uses GeckoLib for the expanding-dome animation. Has no collision,
 * no gravity, and auto-removes after the animation finishes.
 */
public class DomainBarrierEntity extends Entity implements GeoEntity {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    /** Total lifetime in ticks (3 seconds = 60 ticks). */
    private static final int LIFETIME_TICKS = 60;
    private int ticksAlive = 0;

    public DomainBarrierEntity(EntityType<?> type, World world) {
        super(type, world);
        this.setNoGravity(true);
        this.noClip = true;
    }

    // ── GeckoLib ───────────────────────────────────────────────────
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "expand_controller", 0, state -> {
            state.setAnimation(RawAnimation.begin().thenPlay("domain_barrier.expand"));
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    // ── Entity lifecycle ───────────────────────────────────────────
    @Override
    public void tick() {
        super.tick();
        ticksAlive++;
        if (ticksAlive >= LIFETIME_TICKS) {
            this.discard();
        }
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        // No custom tracked data
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) { }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) { }

    @Override
    public boolean shouldRender(double distance) {
        // Visible from far away since the barrier is large
        return distance < 16384;
    }
}
