package phanastrae.arachne.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import phanastrae.arachne.access.EntityWeavesAccess;
import phanastrae.arachne.weave.WeaveCacheHolder;
import phanastrae.arachne.weave.WeaveControl;

@Mixin(Entity.class)
public class EntityMixin implements EntityWeavesAccess {

    private WeaveCacheHolder arachne_weaveCacheHolder;

    @Override
    public WeaveCacheHolder arachne_getOrCreateEntityWeaveCacheHolder() {
        if(this.arachne_weaveCacheHolder == null) {
            this.arachne_weaveCacheHolder = new WeaveCacheHolder();
        }
        return this.arachne_weaveCacheHolder;
    }

    @Override
    public WeaveCacheHolder arachne_getEntityWeaveCacheHolder() {
        return this.arachne_weaveCacheHolder;
    }

    @Override
    public void arachne_clearWeaveCacheHolder() {
        this.arachne_weaveCacheHolder = null;
    }

    @Override
    public boolean arachne_hasWeaveCacheHolder() {
        return (this.arachne_weaveCacheHolder != null);
    }

    @Inject(method = "baseTick", at = @At("RETURN"))
    private void arachne_tickHeldWeaves(CallbackInfo ci) {
        World world = ((Entity)(Object)this).getWorld();
        world.getProfiler().push("arachneWeave");
        WeaveControl.tickEntityWeaves((Entity)(Object)this);
        world.getProfiler().pop();
    }
}
