package phanastrae.arachne.weave;

import org.jetbrains.annotations.Nullable;
import phanastrae.arachne.Arachne;
import phanastrae.arachne.weave.element.active.ActiveNode;
import phanastrae.arachne.weave.element.built.BuiltNode;
import phanastrae.arachne.weave.element.built.BuiltSettings;
import phanastrae.arachne.weave.element.sketch.SketchSettings;

import java.util.Random;

public class WeaveStateUpdater {

    static final int INTERNAL_MASK = 0x40000000; // marks whether a given node id should be taken from the external state or internal state
    static final int SOLO_MASK = 0x80000000; // marks whether a given node id should be taken from the synced state or the independent (solo) state
    static final int CATEGORY_MASK = INTERNAL_MASK | SOLO_MASK;
    static final int ID_MASK = ~CATEGORY_MASK; // for bitwise and ops

    Random random = new Random(0);

    WeaveState dummyState = new WeaveState();

    BuiltWeave builtWeave;
    WeaveInstance weaveInstance;

    WeaveState inputSyncExternal = dummyState;
    WeaveState inputSyncInternal = dummyState;
    WeaveState inputSoloExternal = dummyState;
    WeaveState inputSoloInternal = dummyState;

    WeaveState outputSyncExternal = dummyState;
    WeaveState outputSyncInternal = dummyState;
    WeaveState outputSoloExternal = dummyState;
    WeaveState outputSoloInternal = dummyState;

    public WeaveStateUpdater(WeaveInstance weaveInstance) {
        this.weaveInstance = weaveInstance;
        this.builtWeave = weaveInstance.builtWeave;
    }

    public BuiltWeave getBuiltWeave() {
        return this.builtWeave;
    }

    public BuiltSettings getSettings() {
        return this.builtWeave.settings;
    }

    public void setSyncExternals(WeaveState in, @Nullable WeaveState out) {
        this.inputSyncExternal = in != null ? in : dummyState;
        this.outputSyncExternal = out != null ? out : dummyState;
    }

    public void setSyncInternals(WeaveState in, @Nullable WeaveState out) {
        this.inputSyncInternal = in != null ? in : dummyState;
        this.outputSyncInternal = out != null ? out : dummyState;
    }

    public void setSoloExternals(@Nullable WeaveState in, @Nullable WeaveState out) {
        this.inputSoloExternal = in != null ? in : dummyState;
        this.outputSoloExternal = out != null ? out : dummyState;
    }

    public void setSoloInternals(@Nullable WeaveState in, @Nullable WeaveState out) {
        this.inputSoloInternal = in != null ? in : dummyState;
        this.outputSoloInternal = out != null ? out : dummyState;
    }

    public void update(int tier) {
        this.random.setSeed(0);

        BuiltSettings settings = this.builtWeave.settings;
        int ITERATIONS = settings.getIterations();
        int ITERATIONS_WIND = settings.getWindIterations();
        if(ITERATIONS_WIND > ITERATIONS) ITERATIONS_WIND = ITERATIONS;
        int j = ITERATIONS_WIND == 0 ? ITERATIONS : ITERATIONS / ITERATIONS_WIND;

        float dt = ITERATIONS == 0 ? 0 : (1/20f) / ITERATIONS;
        for(int i = 0; i < ITERATIONS; i++) {
            if(ITERATIONS_WIND != 0 && i % j == 0) {
                this.updateWind(j, tier);
            }

            this.update(dt, tier);
            if(i == 0) { // take output as input after first iteration
                if(tier == 0) {
                    this.inputSyncExternal = this.outputSyncExternal;
                    this.inputSyncInternal = this.outputSyncInternal;
                } else if(tier == 1) {
                    this.inputSoloExternal = this.outputSoloExternal;
                    this.inputSoloInternal = this.outputSoloInternal;
                }
            }
        }
    }

    void updateWind(int multiplier, int tier) {
        addWindForces(multiplier, tier);
    }

    void update(float dt, int tier) {
        addForces(tier);
        applyForces(dt, tier);
        // TODO: remove or change
        double f = Math.exp(-dt * 2);
        for(ActiveNode node : this.outputSyncExternal.nodes) {
            double f2 = f * Math.exp(-dt * (node.vx*node.vx+node.vy*node.vy+node.vz*node.vz)*f*f * 0.05f);
            node.vx *= f2;
            node.vy *= f2;
            node.vz *= f2;
        }
        meetBounds(tier);
    }

    void addForces(int tier) {
        this.builtWeave.addForces(this, tier);
    }

    void addWindForces(int multiplier, int tier) {
        this.builtWeave.addWindForces(this, multiplier, tier);
    }

    void applyForces(float dt, int tier) {
        if(tier == 0) {
            this.outputSyncExternal.acceptForces(dt, this.inputSyncExternal);
            this.outputSyncInternal.acceptForces(dt, this.inputSyncInternal);
        } else if(tier == 1) {
            this.outputSoloExternal.acceptForces(dt, this.inputSoloExternal);
            this.outputSoloInternal.acceptForces(dt, this.inputSoloInternal);
        }
    }

    void meetBounds(int tier) {
        // TODO
    }

    public ActiveNode getNodeInput(int i) {
        int j = i & ID_MASK;
        switch(i & CATEGORY_MASK) {
            case 0 -> {return this.inputSyncExternal.getNode(j);}
            case INTERNAL_MASK -> {return this.inputSyncInternal.getNode(j);}
            case SOLO_MASK -> {return this.inputSoloExternal.getNode(j);}
            case CATEGORY_MASK -> {return this.inputSoloInternal.getNode(j);}
        }
        return dummyState.getNode(-1);
    }

    public ActiveNode getNodeOutput(int i) {
        int j = i & ID_MASK;
        switch(i & CATEGORY_MASK) {
            case 0 -> {return this.outputSyncExternal.getNode(j);}
            case INTERNAL_MASK -> {return this.outputSyncInternal.getNode(j);}
            case SOLO_MASK -> {return this.outputSoloExternal.getNode(j);}
            case CATEGORY_MASK -> {return this.outputSoloInternal.getNode(j);}
        }
        return dummyState.getNode(-1);
    }

    public Random getRandom() {
        return this.random;
    }
}
