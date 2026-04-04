package net.mythic.jjkmod.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * Registers all custom entity types for the JJK mod.
 */
public class ModEntities {

    public static final EntityType<DomainBarrierEntity> DOMAIN_BARRIER =
            Registry.register(
                    Registries.ENTITY_TYPE,
                    Identifier.of("jjk-mod", "domain_barrier"),
                    EntityType.Builder.<DomainBarrierEntity>create(
                                    DomainBarrierEntity::new, SpawnGroup.MISC)
                            .dimensions(0.5f, 0.5f)
                            .maxTrackingRange(128)
                            .trackingTickInterval(1)
                            .build()
            );

    /** Call once from {@link net.mythic.jjkmod.JJKMod#onInitialize()} to force class-load. */
    public static void register() {
        // Static field initializers do the actual registration
    }
}
