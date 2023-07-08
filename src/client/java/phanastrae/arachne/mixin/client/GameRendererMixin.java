package phanastrae.arachne.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import phanastrae.arachne.CameraController;
import phanastrae.arachne.util.TimerHolder;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

	@Inject(method = "render", at = @At("HEAD"))
	void arachne_render(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
		TimerHolder.getInstance().tickAllTimers();
	}

	@Inject(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;update(Lnet/minecraft/world/BlockView;Lnet/minecraft/entity/Entity;ZZF)V", shift = At.Shift.AFTER))
	private void arachne_updatecamera(float tickDelta, long limitTime, MatrixStack matrices, CallbackInfo ci) {
		CameraController.getInstance().UpdateCamera(tickDelta, MinecraftClient.getInstance().gameRenderer.getCamera());
	}

	@Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
	private void arachne_overridefov(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Double> cir) {
		if(CameraController.getInstance().shouldOverrideFOV()) {
			cir.setReturnValue(CameraController.getInstance().getFOV(cir.getReturnValue()));
		}
	}
}