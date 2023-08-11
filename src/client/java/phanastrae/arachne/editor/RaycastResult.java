package phanastrae.arachne.editor;

import net.minecraft.util.math.Vec3d;
import phanastrae.arachne.weave.element.sketch.SketchElement;

public record RaycastResult(SketchElement element, Vec3d pos, double distance) {
}
