package phanastrae.arachne.weave;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;
import phanastrae.arachne.Arachne;
import phanastrae.arachne.access.EntityWeavesAccess;
import phanastrae.arachne.entity.WeaveEntity;
import phanastrae.arachne.setup.ModItems;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class WeaveControl {

    public static Function<NbtCompound, Weave> newWeaveFunction = Weave::new; // this is set to PhysicsSystem::new in client // TODO: change how this works?

    public static void tickEntityWeaves(Entity entity) {
        if(entity.getWorld() == null || !entity.getWorld().isClient()) {
            // exit if not clientside
            return;
        }
        // exit if entity cannot hold weaves
        if(!canHoldWeaves(entity)) return;

        // load newly added entity weaves and unload removed weaves
        loadEntityWeaves(entity);

        // TODO: consider splitting physicsTicks between different frames instead of doing all at once for slightly smoother movement
        // tick all tickable weaves
        int STEPS = 8;
        double dt = 1/20f;
        forEachWeaveInEntity(entity, ((string, weaveCache) -> weaveCache.tick(dt, STEPS)));
    }

    public static boolean canHoldWeaves(Entity entity) {
        if(entity instanceof WeaveEntity) {
            return true;
        }

        if(entity instanceof LivingEntity && entity instanceof EntityWeavesAccess) {
            return true;
        } else {
            return false;
        }
    }

    public static void loadEntityWeaves(Entity entity) {
        if(!(entity instanceof EntityWeavesAccess ewa)) return;
        WeaveCacheHolder wch = ewa.arachne_getEntityWeaveCacheHolder();

        // nbts and strings must be same length
        NbtCompound[] nbts;
        String[] strings;
        if(entity instanceof WeaveEntity weaveEntity) {
            // load weaveEntity weave
            nbts = new NbtCompound[]{weaveEntity.getWeaveNbt()};
            strings = new String[]{"weave"};
        } else if(entity instanceof LivingEntity livingEntity){
            return;
            // TODO: properly implement rendering for entities
            //nbts = new NbtCompound[]{getWeaveNbtFromItem(livingEntity.getMainHandStack())};
            //strings = new String[]{"mainHand"};
        } else {
            return;
        }

        boolean allNull = Arrays.stream(nbts).allMatch((Objects::isNull));
        if(allNull) {
            // remove WCH if needed
            if(wch != null) {
                ewa.arachne_clearWeaveCacheHolder();
            }
        } else {
            // add WCH if needed
            if(wch == null) {
                wch = ewa.arachne_getOrCreateEntityWeaveCacheHolder();
            }

            for(int i = 0; i < nbts.length; i++) {
                WeaveCache cacheMainHand = wch.getOrCreateWeaveCache(strings[i]);
                cacheMainHand.getOrMakeWeave(nbts[i], newWeaveFunction);
            }
        }
    }

    @Nullable
    public static NbtCompound getWeaveNbtFromItem(ItemStack itemStack) {
        if(itemStack == null || itemStack.isEmpty() || !itemStack.isOf(ModItems.WEAVE)) return null;

        NbtCompound nbt = itemStack.getNbt();
        if(nbt == null || nbt.isEmpty() || !nbt.contains("weaveData")) return null;
        NbtCompound nbtSketch = nbt.getCompound("weaveData");
        if(nbtSketch == null || nbtSketch.isEmpty()) return null;
        return nbtSketch;
    }

    public static void forEachWeaveInEntity(Entity entity, BiConsumer<String, WeaveCache> action) {
        if(!(entity instanceof EntityWeavesAccess ewa)) return;

        WeaveCacheHolder wch = ewa.arachne_getEntityWeaveCacheHolder();
        if(wch == null) return;

        wch.forEach(action);
    }
}
