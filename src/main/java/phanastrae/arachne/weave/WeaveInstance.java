package phanastrae.arachne.weave;

import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import phanastrae.arachne.Arachne;
import phanastrae.arachne.weave.element.built.BuiltRenderLayer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class WeaveInstance {

    boolean updating = false;
    boolean updatingBuffer = false;
    public boolean bufferReady = false;

    final Object syncObject = new Object();
    final Object syncObject2 = new Object();

    Lock lock = new ReentrantLock();

    BuiltWeave builtWeave;
    WeaveStateUpdater weaveStateUpdater;
    WeaveStateUpdater lerpWeaveStateUpdater;

    public Map<BuiltRenderLayer, Object> layerToBufferHolderMap = new HashMap<>();

    // TODO actually separate into the 4 states, currently just using worldState for everything and treating it as soloState should be
    // the externally input parts of the state i.e. entity position, other entities' positions
    WeaveState worldState;
    WeaveState prevWorldState;
    WeaveState lerpWorldState;

    // the internal state, that exists on both client and server
    WeaveState serverSyncState;
    WeaveState prevServerSyncState;
    WeaveState lerpServerSyncState;

    // the client-side only externally input parts of the state i.e. player model part positions
    WeaveState clientWorldState;
    WeaveState prevClientWorldState;
    WeaveState lerpClientWorldState;

    // the internal state, that only exists on the client
    WeaveState soloState;
    WeaveState prevSoloState;
    WeaveState lerpSoloState;

    Vec3d wind = Vec3d.ZERO;
    public double windRotation = 0;

    public WeaveInstance(BuiltWeave builtWeave) {
        this.builtWeave = builtWeave;
        this.weaveStateUpdater = new WeaveStateUpdater(this);
        this.lerpWeaveStateUpdater = new WeaveStateUpdater(this);

        this.worldState = new WeaveState(builtWeave.nodes);
        this.prevWorldState = new WeaveState(builtWeave.nodes);
        this.lerpWorldState = new WeaveState(builtWeave.nodes);

        this.serverSyncState = new WeaveState();
        this.prevServerSyncState = new WeaveState();
        this.lerpServerSyncState = new WeaveState();

        this.clientWorldState = new WeaveState();
        this.prevClientWorldState = new WeaveState();
        this.lerpClientWorldState = new WeaveState();

        this.soloState = new WeaveState();
        this.prevSoloState = new WeaveState();
        this.lerpSoloState = new WeaveState();

        setupStateUpdateForRead();
    }

    public void lock() {
        this.lock.lock();
    }

    public void unlock() {
        this.lock.unlock();
    }

    public void preUpdate(World world) {
        swapStates();

        if(world != null) {
            gatherWorldData(world);
        }
    }

    public void update() {
        this.lock();
        try {
            updateSyncStates();
            updateSoloStates();
        } finally {
            this.unlock();
        }
        this.setUpdating(false);
    }

    void updateSyncStates() {
        this.weaveStateUpdater.setSyncExternals(prevWorldState, worldState);
        this.weaveStateUpdater.setSyncInternals(prevServerSyncState, serverSyncState);
        this.weaveStateUpdater.setSoloExternals(null, null);
        this.weaveStateUpdater.setSoloInternals(null, null);

        this.weaveStateUpdater.update(0);
        // TODO server syncing
    }

    void updateSoloStates() {
        this.weaveStateUpdater.setSyncExternals(prevWorldState, null);
        this.weaveStateUpdater.setSyncInternals(prevServerSyncState, null);
        this.weaveStateUpdater.setSoloExternals(prevClientWorldState, clientWorldState);
        this.weaveStateUpdater.setSoloInternals(prevSoloState, soloState);

        this.weaveStateUpdater.update(1);
    }

    void setupStateUpdateForRead() {
        this.lerpWeaveStateUpdater.setSyncExternals(lerpWorldState, null);
        this.lerpWeaveStateUpdater.setSyncInternals(lerpServerSyncState, null);
        this.lerpWeaveStateUpdater.setSoloExternals(lerpClientWorldState, null);
        this.lerpWeaveStateUpdater.setSoloInternals(lerpSoloState, null);
    }

    public void swapStates() {
        WeaveState temp = this.worldState;
        this.worldState = this.prevWorldState;
        this.prevWorldState = temp;

        temp = this.serverSyncState;
        this.serverSyncState = this.prevServerSyncState;
        this.prevServerSyncState = temp;

        temp = this.clientWorldState;
        this.clientWorldState = this.prevClientWorldState;
        this.prevClientWorldState = temp;

        temp = this.soloState;
        this.soloState = this.prevSoloState;
        this.prevSoloState = temp;
    }

    public void doLerp(float lerp) {
        this.lerpWorldState.lerp(prevWorldState, worldState, lerp);
        this.lerpServerSyncState.lerp(prevServerSyncState, serverSyncState, lerp);
        this.lerpClientWorldState.lerp(prevClientWorldState, clientWorldState, lerp);
        this.lerpSoloState.lerp(prevSoloState, soloState, lerp);
    }

    public void gatherWorldData(World world) {
        double t = world.getTime() / 20f;
        this.wind = new Vec3d(- 5 + Math.sin(t / 16) * Math.sin(t / 7) * 3, Math.sin(t / 3.314) * 2, Math.cos(t / 16) * Math.sin(t / 7) * 8);
        this.wind = this.wind.rotateY((float)Math.toRadians(this.windRotation));
    }
    public void setUpdating() {
        setUpdating(true);
    }

    public void setUpdating(boolean updating) {
        synchronized (syncObject) {
            this.updating = updating;
            if(!updating) {
                syncObject.notifyAll();
            }
        }
    }

    public void waitForUpdate() {
        if(!this.updating) {
            return;
        }
        try {
            synchronized (syncObject) {
                while(this.updating) {
                    syncObject.wait();
                }
            }
        } catch (InterruptedException ignored) {
        }
    }
    public void setBufferUpdating() {
        setBufferUpdating(true);
    }

    public void setBufferUpdating(boolean updatingBuffer) {
        synchronized (syncObject2) {
            this.updatingBuffer = updatingBuffer;
            if(!updatingBuffer) {
                syncObject2.notifyAll();
            }
        }
    }

    public void waitForBufferUpdate() {
        if(!this.updatingBuffer) {
            return;
        }
        try {
            synchronized (syncObject2) {
                while(this.updatingBuffer) {
                    syncObject2.wait();
                }
            }
        } catch (InterruptedException ignored) {
        }
    }
}
