package phanastrae.arachne.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import phanastrae.arachne.render.BufferHolders;
import phanastrae.arachne.weave.WeaveRenderer;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    @Shadow
    private ClientWorld world;

    @Shadow @Final private EntityRenderDispatcher entityRenderDispatcher;

    @Shadow private Frustum frustum;

    @Inject(method = "reload()V", at = @At("HEAD"))
    public void arachne_onReload(CallbackInfo ci) {
        BufferHolders.releaseAll();
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/BufferBuilderStorage;getEntityVertexConsumers()Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;"))
    public void arachne_setupBufferUpdates(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f projectionMatrix, CallbackInfo ci) {
        Profiler profiler = MinecraftClient.getInstance().getProfiler();
        profiler.push("arachne_queueUpdates");
        Vec3d v = camera.getPos();
        double d = v.x;
        double e = v.y;
        double f = v.z;
        for(Entity entity : this.world.getEntities()) {
            if(!this.entityRenderDispatcher.shouldRender(entity, frustum, d, e, f)) continue;

            WeaveRenderer.updateEntityWeaves(entity, tickDelta, matrices, this.entityRenderDispatcher.getLight(entity, tickDelta));
        }
        profiler.pop();
    }
}
