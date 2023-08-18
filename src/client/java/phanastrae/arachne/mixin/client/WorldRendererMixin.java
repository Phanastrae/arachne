package phanastrae.arachne.mixin.client;

import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import phanastrae.arachne.render.BufferHolders;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    @Inject(method = "reload()V", at = @At("HEAD"))
    public void arachne_onReload(CallbackInfo ci) {
        BufferHolders.releaseAll();
    }
}
