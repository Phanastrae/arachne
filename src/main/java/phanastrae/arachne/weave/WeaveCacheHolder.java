package phanastrae.arachne.weave;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.function.BiConsumer;

public class WeaveCacheHolder {

    private final HashMap<String, WeaveCache> map = new HashMap<>();

    public WeaveCache getOrCreateWeaveCache(String id) {
        if(this.map.containsKey(id)) {
            return this.map.get(id);
        } else {
            WeaveCache wc = new WeaveCache();
            this.map.put(id, wc);
            return wc;
        }
    }

    @Nullable
    public WeaveCache getWeaveCache(String id) {
        return this.map.getOrDefault(id, null);
    }

    public void forEach(BiConsumer<String, WeaveCache> action) {
        this.map.forEach(action);
    }
}
