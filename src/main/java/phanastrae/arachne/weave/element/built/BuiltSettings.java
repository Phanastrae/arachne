package phanastrae.arachne.weave.element.built;

import phanastrae.arachne.weave.element.sketch.SketchSettings;

public class BuiltSettings {

    int stepMain;
    int stepWind;
    double windMultiplier;
    double gravityMultiplier;
    boolean doCulling;
    double width;
    double height;

    public BuiltSettings(SketchSettings settings) {
        this.stepMain = settings.getStepMain();
        this.stepWind = settings.getStepWind();
        this.windMultiplier = settings.getWindMultiplier();
        this.gravityMultiplier = settings.getGravityMultiplier();
        this.doCulling = settings.getDoCulling();
        this.width = settings.getWidth();
        this.height = settings.getHeight();
    }

    public int getIterations() {
        if(this.stepMain < 0) return 0;
        return 1 << this.stepMain;
    }

    public int getWindIterations() {
        if(this.stepWind < 0) return 0;
        return 1 << this.stepWind;
    }

    public double getWindMultiplier() {
        return this.windMultiplier;
    }

    public double getGravityMultiplier() {
        return this.gravityMultiplier;
    }

    public boolean getDoCulling() {
        return this.doCulling;
    }

    public double getHeight() {
        return this.height;
    }

    public double getWidth() {
        return this.width;
    }
}
