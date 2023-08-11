package phanastrae.arachne;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.joml.Math;
import org.joml.Vector2d;
import phanastrae.arachne.screen.editor.EditorMainScreen;
import phanastrae.arachne.util.ArachneMath;
import phanastrae.arachne.util.CenteredPlane;
import phanastrae.arachne.util.Line;

public class CameraController {
    private static final CameraController INSTANCE = new CameraController(); // TODO: make 1 instance per client? not needed but might be good?
    public static CameraController getInstance() {
        return INSTANCE;
    }

    public static final float TRANSITION_TIME = 0.25f;
    private float lerp = 0;
    public Vec3d targetPos = Vec3d.ZERO;
    public Vec3d originPos = Vec3d.ZERO;
    public float pitch = 0;
    public float yaw = 0;
    public float zoom = 1;
    public Vec3d oldPos = Vec3d.ZERO;
    public float oldPitch = 0;
    public float oldYaw = 0;
    public boolean RETURN_TO_PLAYER = false;
    public boolean NEEDS_TARGET_UPDATE = false;
    public boolean USING_ALT_CAMERA_MODE = false; //TODO: save? maybe?

    public boolean jumpHeld = false;
    public boolean sneakHeld = false;
    public boolean forwardHeld = false;
    public boolean backwardHeld = false;
    public boolean leftHeld = false;
    public boolean rightHeld = false;

    public void UpdateCamera(float tickDelta, Camera camera) { //TODO: remove tickDelta? not sure it's needed as all our stuff here seems to be based on framerate deltatime and not 20hz deltatime
        if(this.NEEDS_TARGET_UPDATE) {
            updateTarget();
        }
        double dt = MinecraftClient.getInstance().getLastFrameDuration() / 20;
        updateLerp(dt);
        updateTargetPosition(dt);
        if(this.lerp > 0) {
            // TODO: reduce unnecessary recalcs if possible? maybe??
            lerpCamera(camera);
        }

        PlayerEntity player = MinecraftClient.getInstance().player;
        if(player != null) {
            // TODO: check this works on server
            if(MinecraftClient.getInstance().currentScreen instanceof EditorMainScreen screen) {
                CenteredPlane plane = new CenteredPlane(this.targetPos, getCameraLookVector(camera));
                Line line = screen.editorInstance.getMouseRay(screen.mouseXtoScreenSpace(screen.lastMouseX),screen.mouseYtoScreenSpace(screen.lastMouseY));
                Line lineGlobal = new Line(screen.localToGlobal(line.point), line.offset);
                Vec3d intersect = plane.intersectLine(lineGlobal, 0);

                if(intersect == null) {
                    intersect = targetPos;
                }

                // TODO: tweak values and how this works, it works okish but is not ideal
                playerLookEyes(player, intersect, 10 * this.lerp);
            }
        }
    }

    public void updateTarget() {
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null) {
            this.pitch = player.getPitch();
            this.yaw = player.getYaw();
            this.originPos = player.getPos();
        }
        Screen screen = MinecraftClient.getInstance().currentScreen;
        if(screen instanceof EditorMainScreen screenML) {
            Vec3i blockPos = screenML.getScreenHandler().getPosition();
            this.targetPos = new Vec3d(blockPos.getX() + 0.5f, blockPos.getY() + 1.5f, blockPos.getZ() + 0.5f);
            this.originPos = this.targetPos;
        }
        this.zoom = 1;
        this.NEEDS_TARGET_UPDATE = false;

        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
        if(camera != null) {
            this.oldPos = camera.getPos();
            this.oldPitch = camera.getPitch();
            this.oldYaw = camera.getYaw();
        } else {
            this.oldPos = this.targetPos;
            this.oldPitch = this.pitch;
            this.oldYaw = this.yaw;
        }
        this.RETURN_TO_PLAYER = false;
    }

    public void updateLerp(double dt) {
        Screen screen = MinecraftClient.getInstance().currentScreen;
        if(screen instanceof EditorMainScreen) {
            this.lerp += dt / TRANSITION_TIME;
        } else {
            this.lerp -= dt / TRANSITION_TIME;
        }
        this.lerp = Math.clamp(0, 1, lerp);
    }

    public void updateTargetPosition(double dt) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if(player == null) return;
        Input input = player.input;
        if(input == null) return;

        double x = (leftHeld ? 1 : 0) + (rightHeld ? -1 : 0);
        double y = (jumpHeld ? 1 : 0) + (sneakHeld ? -1 : 0);
        double z = (forwardHeld ? 1 : 0) + (backwardHeld ? -1 : 0);
        float moveSpeed = 0.8f + zoom * 2;
        Vec3d vec = new Vec3d(x, 0, z);
        if(USING_ALT_CAMERA_MODE) {
            vec = vec.add(0, y, 0);
            vec = vec.rotateX(Math.toRadians(-pitch));
            vec = vec.rotateY(Math.toRadians(-yaw));
        } else {
            vec = vec.rotateY(Math.toRadians(-yaw));
            vec = vec.add(0, y, 0);
        }
        vec = vec.normalize();
        vec = vec.multiply(moveSpeed);
        targetPos = targetPos.add(vec.multiply(dt));
        Vec3d offset = targetPos.subtract(this.originPos);
        float maxDistance = 15; // TODO: tweak how this works
        if(offset.lengthSquared() > maxDistance * maxDistance) {
            offset = offset.multiply(maxDistance / offset.length());
            targetPos = this.originPos.add(offset);
        }
    }

    public void lerpCamera(Camera camera) {
        float t = smoothLerp(lerp);
        lerpRotation(camera, t);
        lerpPosition(camera, t);
    }

    public void lerpRotation(Camera camera, float t) {
        float startYaw = this.oldYaw;
        float startPitch = this.oldPitch;
        if(this.RETURN_TO_PLAYER) {
            startYaw = camera.getYaw();
            startPitch = camera.getPitch();
        }
        float newPitch = Math.lerp(startPitch, this.pitch, t);
        float newYaw = MathHelper.lerpAngleDegrees(t, startYaw, this.yaw);
        camera.setRotation(newYaw, newPitch);
    }

    public void lerpPosition(Camera camera, float t) {
        float yaw = Math.toRadians(camera.getYaw());
        float pitch = Math.toRadians(camera.getPitch());

        Vec3d targetPos2 = new Vec3d(targetPos.x + Math.sin(yaw) * Math.cos(pitch) * zoom, targetPos.y + Math.sin(pitch) * zoom, targetPos.z - Math.cos(yaw) * Math.cos(pitch) * zoom);
        Vec3d startPos = this.oldPos;
        if(this.RETURN_TO_PLAYER) {
            startPos = camera.getPos();
        }
        Vec3d lerpPos = ArachneMath.lerp(startPos, targetPos2, t);
        camera.setPos(lerpPos.x, lerpPos.y, lerpPos.z);
    }

    public void mouseDragged(double deltaX, double deltaY) {
        this.pitch += deltaY;
        this.yaw += deltaX;
        this.pitch = Math.clamp(-90, 90, this.pitch);
        this.yaw = this.yaw % 360;
    }

    public void mouseScrolled(double amount) {
        this.zoom += amount * -0.1f;
        this.zoom = Math.clamp(0.1f, 3, this.zoom);
    }

    public boolean shouldCancelHUD() {
        //TODO: this stops lots of GUI i.e. chat, is this ideal?
        return (MinecraftClient.getInstance().currentScreen instanceof EditorMainScreen) || lerp > 0.25f;
    }

    public boolean shouldHideHeldItem() {
        return shouldCancelHUD();
    }

    public boolean shouldForceRenderCameraEntity() {
        // TODO: maybe tweak in future?
        return lerp > 0.5f;
    }

    public boolean shouldOverrideFOV() {
        return lerp > 0;
    }

    public boolean shouldCancelOverlays() {
        return shouldCancelHUD();
    }

    public double getFOV(double original) {
        return Math.lerp(original, 70, lerp);
    }

    public static float smoothLerp(float lerp) {
        if(lerp < 0.5) {
            return 2*lerp*lerp;
        }
        else return 1 - 2 * (lerp - 1) * (lerp - 1);
    }

    public void onScreenOpen() {
        this.NEEDS_TARGET_UPDATE = true;
        this.jumpHeld = false;
        this.sneakHeld = false;
        this.forwardHeld = false;
        this.backwardHeld = false;
        this.leftHeld = false;
        this.rightHeld = false;
    }

    public static Vec3d getCameraLookVector(Camera camera) {
        double yaw = -Math.toRadians(camera.getYaw());
        double pitch = -Math.toRadians(camera.getPitch());
        return new Vec3d(Math.sin(yaw) * Math.cos(pitch), Math.sin(pitch), Math.cos(yaw) * Math.cos(pitch));
    }

    public static void playerLookEyes(PlayerEntity player, Vec3d target, double maxAngleChangeDegrees) {
        Vector2d yawPitch = calcLookYawPitch(player.getEyePos(), target);
        // keep player head from turning backwards too far or moving too quickly
        double yaw = clampAngleAroundValue(yawPitch.x, player.getHeadYaw(), maxAngleChangeDegrees);
        double pitch = clampAngleAroundValue(yawPitch.y, player.getPitch(), maxAngleChangeDegrees); //TODO: Look into leaning pitch, check if relevant
        pitch = clampAngle(pitch, -85, 85);

        player.prevHeadYaw = player.getHeadYaw();
        player.setHeadYaw((float)yaw);
        player.prevYaw = player.getYaw();
        player.setYaw((float)yaw);
        player.prevPitch = player.getPitch();
        player.setPitch((float)pitch);
    }

    public static Vector2d calcLookYawPitch(Vec3d root, Vec3d target) { // TODO: keep an eye out for weird rapid movement
        Vec3d offset = target.subtract(root).normalize();
        double yaw = Math.toDegrees(Math.atan2(-offset.x, offset.z));
        double pitch = Math.toDegrees(-Math.asin(offset.y));
        // set infinite and NaN values to 0 just in case
        if(!Double.isFinite(yaw)) yaw = 0;
        if(!Double.isFinite(pitch)) pitch = 0;
        return new Vector2d(yaw, pitch); //TODO
    }

    public static double clampAngleAroundValue(double val, double center, double change) {
        return clampAngle(val, center - change, center + change);
    }

    public static double clampAngle(double val, double min, double max) {
        // get the offset of the val from the min (in the range [0-360])
        double valOffset = (val - min) % 360;
        if(valOffset < 0) valOffset += 360;
        // get the offset of the max from the min (in the range [0-360])
        double maxOffset = (max - min) % 360;
        if(maxOffset < 0) maxOffset += 360;
        // if offset is in range [0, maxOffset] return (valOffset + min)
        if(valOffset <= maxOffset) {
            return val;
        }
        // otherwise clamp to nearest bound

        // get the range of invalid values
        double invalidRange = 360 - (maxOffset);
        // if offset is in range [maxOffset, maxOffset + invalidRange / 2] (closer to max), return max
        if(valOffset <= maxOffset + invalidRange / 2) {
            return max;
        } else { // else in range [maxOffset + invalidRange / 2, 360], so return min
            return min;
        }
    }
}
