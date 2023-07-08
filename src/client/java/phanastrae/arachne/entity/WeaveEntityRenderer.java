package phanastrae.arachne.entity;

import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class WeaveEntityRenderer extends EntityRenderer<WeaveEntity> {

    public WeaveEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(WeaveEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        // rendering done in WeaveRenderer
    }

    @Override
    public Identifier getTexture(WeaveEntity entity) {
        return MissingSprite.getMissingSpriteId();
    }

    // TODO: culling
    @Override
    public boolean shouldRender(WeaveEntity entity, Frustum frustum, double x, double y, double z) {
        return true;
    }
}
