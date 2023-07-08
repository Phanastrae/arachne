package phanastrae.arachne.mixin.client;

import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import phanastrae.arachne.CameraController;

@Mixin(Camera.class)
public class CameraMixin {
    @Inject(method = "isThirdPerson", at = @At("HEAD"), cancellable = true)
    public void arachne_isthirdperson(CallbackInfoReturnable<Boolean> cir) {
        // in vanilla, this method is apparently only called when checking whether to render the camera entity
        // all other first/third person logic is located elsewhere
        if(CameraController.getInstance().shouldForceRenderCameraEntity()) {
            cir.setReturnValue(true);
        }
    }
}
