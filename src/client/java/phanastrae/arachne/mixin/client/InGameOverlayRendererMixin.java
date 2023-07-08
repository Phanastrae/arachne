package phanastrae.arachne.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import phanastrae.arachne.CameraController;

@Mixin(InGameOverlayRenderer.class)
public class InGameOverlayRendererMixin {
    @Inject(method = "renderOverlays", at = @At("HEAD"), cancellable = true)
    private static void arachne_cancelOverlays(MinecraftClient client, MatrixStack matrices, CallbackInfo ci) {
        if(CameraController.getInstance().shouldCancelOverlays()) {
            ci.cancel();
        }
    }
}
