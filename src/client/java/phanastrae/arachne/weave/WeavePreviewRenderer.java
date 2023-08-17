package phanastrae.arachne.weave;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import phanastrae.arachne.Arachne;
import phanastrae.arachne.weave.element.built.BuiltSettings;
import phanastrae.old.Weave;

public class WeavePreviewRenderer {

    public static void render(@Nullable WeaveInstance weave, DrawContext context, int x, int y, int width, int height, int borderColor) {
        if(weave == null) {
            return;
        }

        VertexConsumerProvider vertexConsumers = context.getVertexConsumers();
        vertexConsumers.getBuffer(RenderLayer.getSolid()); // force draw TODO is this the way to do this

        context.enableScissor(x, y, x + width, y + height);
        //RenderSystem.enableCull(); // TODO do cull and depth test do anything here and are they right?
        //RenderSystem.enableDepthTest();

        MatrixStack matrices = context.getMatrices();

        matrices.push();
        matrices.translate(x + width / 2f, y + height / 2f, 0);
        matrices.multiplyPositionMatrix(new Matrix4f().scale(width, -height, -1));
        matrices.push();
        matrices.peek().getPositionMatrix().mulAffine(new Matrix4f().setPerspective((float)(70 * 180 / Math.PI), 1, 0.05f, 256));

        int t = 0;
        if(MinecraftClient.getInstance().player != null) {
            t = MinecraftClient.getInstance().player.age;
        }
        matrices.translate(0, 0, 8);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(30));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((-t * 2) % 360));
        vertexConsumers.getBuffer(RenderLayer.getSolid()); // force draw TODO is this the way to do this

        // scale preview
        float scaleFactor = (float)(0.75 / (weave.builtWeave.getSmallestEncompassingCubeWidth() / 2));
        if(Float.isFinite(scaleFactor)) {
            if(scaleFactor < 0.01f) {
                scaleFactor = 0.01f;
            }
            matrices.scale(scaleFactor, scaleFactor, scaleFactor);
        }

        WeaveRenderer.renderInstance(weave, 0, matrices, vertexConsumers, LightmapTextureManager.MAX_LIGHT_COORDINATE, 0); // TODO overlay?
        matrices.pop();
        matrices.pop();
        vertexConsumers.getBuffer(RenderLayer.getSolid()); // force draw TODO is this the way to do this
        //RenderSystem.disableDepthTest();
        //RenderSystem.disableCull();
        context.disableScissor();
        if(borderColor != -1) {
            context.drawBorder(x - 1, y - 1, width + 2, height + 2, borderColor);
        }
    }

    public static void render(@Nullable Weave weave, DrawContext context, int x, int y, int width, int height, int borderColor) {
        // TODO doesn't look right on quilt
        if(weave == null) {
            return;
        }

        VertexConsumerProvider vertexConsumers = context.getVertexConsumers();
        vertexConsumers.getBuffer(RenderLayer.getSolid()); // force draw TODO is this the way to do this

        //context.enableScissor(x, y, x + width, y + height);
        //RenderSystem.enableCull(); // TODO do cull and depth test do anything here and are they right?
        //RenderSystem.enableDepthTest();

        MatrixStack matrices = context.getMatrices();

        matrices.push();
        matrices.translate(x + width / 2f, y + height / 2f, 0);
        matrices.multiplyPositionMatrix(new Matrix4f().scale(width, -height, -1));
        matrices.push();
        matrices.peek().getPositionMatrix().mulAffine(new Matrix4f().setPerspective((float)(70 * 180 / Math.PI), 1, 0.05f, 256));

        int t = 0;
        if(MinecraftClient.getInstance().player != null) {
            t = MinecraftClient.getInstance().player.age;
        }
        matrices.translate(0, 0, 8);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(30));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((-t * 2) % 360));
        VertexConsumer vcLines = vertexConsumers.getBuffer(RenderLayer.LINES);
        for(int i = -24; i <= 24; i++) {
            for(int j = -24; j <= 24; j++) {
                // TODO: set origin to actual base?
                // TODO: make depth test actually work so i can have gridlines?
                //WeaveRenderer.renderLine(vcLines, matrices, new Vec3d(i/4f, -0.5, -6), new Vec3d(i/4f, -0.5, 6), 255, 255 ,255,(int)(127/25f * (25 - Math.abs(i))));
                //WeaveRenderer.renderLine(vcLines, matrices, new Vec3d(-6, -0.5, i/4f), new Vec3d(6, -0.5, i/4f), 255, 255 ,255,(int)(127/25f * (25 - Math.abs(i))));
            }
        }
        vertexConsumers.getBuffer(RenderLayer.getSolid()); // force draw TODO is this the way to do this
        WeaveRenderer.render(weave, 0, matrices, vertexConsumers, LightmapTextureManager.MAX_LIGHT_COORDINATE, 0); // TODO should overlay be different?
        matrices.pop();
        matrices.pop();
        vertexConsumers.getBuffer(RenderLayer.getSolid()); // force draw TODO is this the way to do this
        //RenderSystem.disableDepthTest();
        //RenderSystem.disableCull();
        //context.disableScissor();
        if(borderColor != -1) {
            context.drawBorder(x - 1, y - 1, width + 2, height + 2, borderColor);
        }
    }
}
