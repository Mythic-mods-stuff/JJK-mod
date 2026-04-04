package net.mythic.jjkmod.client.model;

import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import net.mythic.jjkmod.entity.DomainBarrierEntity;

/**
 * GeckoLib model for the domain expansion barrier.
 *
 * <p>Uses the {@link DefaultedEntityGeoModel} convention so asset paths are:
 * <ul>
 *   <li>Geo:       {@code assets/jjk-mod/geo/entity/domain_barrier.geo.json}</li>
 *   <li>Texture:   {@code assets/jjk-mod/textures/entity/domain_barrier.png}</li>
 *   <li>Animation: {@code assets/jjk-mod/animations/entity/domain_barrier.animation.json}</li>
 * </ul>
 */
public class DomainBarrierModel extends DefaultedEntityGeoModel<DomainBarrierEntity> {

    public DomainBarrierModel() {
        super(Identifier.of("jjk-mod", "domain_barrier"));
    }
}
