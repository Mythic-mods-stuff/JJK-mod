package net.mythic.jjkmod.client.renderer;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import net.mythic.jjkmod.client.model.DomainBarrierModel;
import net.mythic.jjkmod.entity.DomainBarrierEntity;

/**
 * Renders the domain expansion barrier with translucent blending
 * so the dome effect is see-through.
 */
public class DomainBarrierRenderer extends GeoEntityRenderer<DomainBarrierEntity> {

    public DomainBarrierRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new DomainBarrierModel());
    }

    @Override
    public RenderLayer getRenderType(DomainBarrierEntity entity,
                                     Identifier texture,
                                     @org.jetbrains.annotations.Nullable net.minecraft.client.render.VertexConsumerProvider bufferSource,
                                     float partialTick) {
        return RenderLayer.getEntityTranslucent(texture);
    }
}
