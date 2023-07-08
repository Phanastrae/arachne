package phanastrae.arachne.access;

import phanastrae.arachne.weave.WeaveCacheHolder;

public interface EntityWeavesAccess {
    WeaveCacheHolder arachne_getOrCreateEntityWeaveCacheHolder();
    WeaveCacheHolder arachne_getEntityWeaveCacheHolder();
    void arachne_clearWeaveCacheHolder();
    boolean arachne_hasWeaveCacheHolder();
}
