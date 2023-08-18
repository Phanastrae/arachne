package phanastrae.arachne.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import phanastrae.arachne.render.BufferHolders;
import phanastrae.arachne.screen.ArachneTabResources;

import java.util.concurrent.CompletableFuture;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(method = "reloadResources(Z)Ljava/util/concurrent/CompletableFuture;", at = @At("HEAD"))
    public void arachne_markReloadRequired(boolean force, CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        ArachneTabResources.needsReload = true;
    }

    @Inject(method = "run", at = @At("RETURN"))
    public void arachne_cleanupOnClose(CallbackInfo ci) {
        BufferHolders.releaseAll();
    }

    @Inject(method = "cleanUpAfterCrash", at = @At("HEAD"))
    public void arachne_cleanupAfterCrash(CallbackInfo ci) {
        BufferHolders.releaseAll();
    }

    @Inject(method = "render", at = @At("HEAD"))
    public void arachne_tidyBuffers(boolean tick, CallbackInfo ci) {
        Profiler profiler = MinecraftClient.getInstance().getProfiler();
        profiler.push("arachne_tidyBuffers");
        if(BufferHolders.timeFromRelease() > 5E9) { // check to release unused buffers every 5 seconds
            BufferHolders.releaseUnused();
        }
        profiler.pop();
    }
}
