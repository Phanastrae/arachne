package phanastrae.arachne.entity;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import phanastrae.arachne.CameraController;
import phanastrae.arachne.setup.ModItems;
import phanastrae.arachne.weave.WeaveRenderer;

import java.util.function.Function;

public class WeaveEntityRenderer extends EntityRenderer<WeaveEntity> {

    public WeaveEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(WeaveEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        // main rendering done in WeaveRenderer

        if(shouldShowIcon()) {
            VertexConsumer vc = vertexConsumers.getBuffer(RenderLayer.getCutout());

            Function<Identifier, Sprite> ATLAS = MinecraftClient.getInstance().getSpriteAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE);
            Sprite sprite = ATLAS.apply(new Identifier("arachne", "item/weave"));

            Camera cam = MinecraftClient.getInstance().gameRenderer.getCamera();

            matrices.push();
            matrices.translate(0, 0.5, 0);
            matrices.multiply(new Quaternionf().rotateY((float) (Math.PI + Math.toRadians(-cam.getYaw()))));
            matrices.multiply(new Quaternionf().rotateX((float) Math.toRadians(-cam.getPitch())));
            matrices.translate(-0.5, -0.5, 0);

            Matrix4f mat = matrices.peek().getPositionMatrix();
            vc.vertex(mat, 0, 0, 0).color(255, 255, 255, 255).texture(sprite.getMinU(), sprite.getMaxV()).overlay(0).light(light).normal(0, 1, 0).next();
            vc.vertex(mat, 1, 0, 0).color(255, 255, 255, 255).texture(sprite.getMaxU(), sprite.getMaxV()).overlay(0).light(light).normal(0, 1, 0).next();
            vc.vertex(mat, 1, 1, 0).color(255, 255, 255, 255).texture(sprite.getMaxU(), sprite.getMinV()).overlay(0).light(light).normal(0, 1, 0).next();
            vc.vertex(mat, 0, 1, 0).color(255, 255, 255, 255).texture(sprite.getMinU(), sprite.getMinV()).overlay(0).light(light).normal(0, 1, 0).next();
            matrices.pop();

            VertexConsumer lines = vertexConsumers.getBuffer(RenderLayer.LINES);
            matrices.push();
            matrices.multiply(new Quaternionf().rotateY((float)Math.toRadians(entity.getExtraNbt().getInt("direction")) * 90));
            Vec3d v1 = Vec3d.ZERO;
            Vec3d v2 = new Vec3d(-1, 0, 0);
            WeaveRenderer.drawLine(lines, matrices, v1.x, v1.y, v1.z, v2.x, v2.y, v2.z, 255, 127, 127, 255);
            matrices.pop();
        }
    }

    public boolean shouldShowIcon() {
        if(MinecraftClient.getInstance().options.debugEnabled) {
            return true;
        }

        PlayerEntity p = MinecraftClient.getInstance().player;
        if(p != null) {
            if(p.isHolding(ModItems.WEAVE) || p.isHolding(ModItems.WEAVE_CONTROLLER)) {
                return true;
            }
        }
        return false;
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
