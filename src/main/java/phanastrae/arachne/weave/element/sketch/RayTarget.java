package phanastrae.arachne.weave.element.sketch;

import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import phanastrae.arachne.util.Line;

public interface RayTarget {

    @Nullable
    Pair<Vec3d, Double> getRayHit(Line ray);
}
