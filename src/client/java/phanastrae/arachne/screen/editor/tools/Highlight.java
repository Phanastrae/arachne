package phanastrae.arachne.screen.editor.tools;

import net.minecraft.util.math.Vec3d;

public class Highlight {
    final double xStart;
    final double yStart;
    double xEnd;
    double yEnd;

    public Highlight(double xStart, double yStart) {
        this.xStart = xStart;
        this.yStart = yStart;
        setEnd(xStart, yStart);
    }

    public void setEnd(double xEnd, double yEnd) {
        this.xEnd = xEnd;
        this.yEnd = yEnd;
    }

    public Vec3d getMinPos() {
        return new Vec3d(Math.min(xStart, xEnd), Math.min(yStart, yEnd), 0);
    }

    public Vec3d getMaxPos() {
        return new Vec3d(Math.max(xStart, xEnd), Math.max(yStart, yEnd), 0);
    }
}
