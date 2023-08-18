package phanastrae.arachne;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import phanastrae.arachne.render.ModShaders;
import phanastrae.arachne.screen.ArachneTabResources;
import phanastrae.arachne.screen.MysticLoomScreen;
import phanastrae.arachne.setup.ModBlocks;
import phanastrae.arachne.block.SketchingTableModelProvider;
import phanastrae.arachne.block.SketchingTableRenderer;
import phanastrae.arachne.setup.ModBlockEntities;
import phanastrae.arachne.block.blockentity.SketchingTableBlockEntity;
import phanastrae.arachne.setup.ModEntities;
import phanastrae.arachne.entity.WeaveEntityRenderer;
import phanastrae.arachne.item.SketchTooltipComponent;
import phanastrae.arachne.item.SketchTooltipData;
import phanastrae.arachne.setup.ModScreenHandlerTypes;
import phanastrae.arachne.screen.editor.EditorIntroScreen;
import phanastrae.arachne.screen.editor.EditorMainScreen;
import phanastrae.arachne.thread.RunnableQueue;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class ArachneClient implements ClientModInitializer {
    public static RunnableQueue runnableQueueClient = new RunnableQueue("arachne_client", 4);

    @Override
    public void onInitializeClient() {
        SketchingTableBlockEntity.TICK_EVENT = EditorMainScreen::tickFromBlockEntity;

        // setup for arachne tab custom reloading
        ItemGroupEvents.modifyEntriesEvent(RegistryKey.of(Registries.ITEM_GROUP.getKey(), Arachne.id("arachne"))).register(entries -> ArachneTabResources.needsReload = false);

        // TODO: cleanup
        // setup screen handlers
        HandledScreens.register(ModScreenHandlerTypes.SKETCHING_TABLE, EditorIntroScreen::new);
        HandledScreens.register(ModScreenHandlerTypes.MYSTIC_LOOM, MysticLoomScreen::new);

        // setup block renderers
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.MYSTIC_LOOM, RenderLayer.getCutout());
        ModelLoadingRegistry.INSTANCE.registerResourceProvider(rm -> new SketchingTableModelProvider());

        // setup block entity renderers
        BlockEntityRendererFactories.register(ModBlockEntities.SKETCHING_TABLE, SketchingTableRenderer::new);

        // setup entity renderers
        EntityRendererRegistry.register(ModEntities.WEAVE_ENTITY, WeaveEntityRenderer::new);

        // cancel block outline if in loom gui
        WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register(listener());
        // register shaders
        CoreShaderRegistrationCallback.EVENT.register(ModShaders::registerShaders);

        TooltipComponentCallback.EVENT.register((data) -> {
            if (data instanceof SketchTooltipData) {
                return new SketchTooltipComponent((SketchTooltipData)data);
            }
            return null;
        });

        ItemGroupEvents.modifyEntriesEvent(RegistryKey.of(Registries.ITEM_GROUP.getKey(), Arachne.id("arachne"))).register(entries -> {
            Arachne.LOGGER.info("Reloaded the Arachne Tab's Prefab Sketches/Weaves");
            ArachneTabResources.addPrefabsToMenu(entries);
        });

        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleResourceReloadListener<List<NbtCompound>>() {
            @Override
            public Identifier getFabricId() {
                return Arachne.id("resource_reload");
            }

            @Override
            public CompletableFuture<List<NbtCompound>> load(ResourceManager manager, Profiler profiler, Executor executor) {
                return CompletableFuture.supplyAsync(() -> {
                    Map<Identifier, List<Resource>> map = manager.findAllResources("arachne_weaves", (a)->true);
                    List<NbtCompound> nbtList = new ArrayList<>();
                    map.forEach((identifier, resources) -> {
                        for(Resource res : resources) {
                            try {
                                InputStream is = res.getInputStream();
                                NbtCompound nbt = NbtIo.readCompressed(is);
                                nbt.putString("fileId", identifier.toString());
                                nbtList.add(nbt);
                                is.close();
                            } catch (IOException ignored) {
                            }
                        }
                    });
                    return nbtList;
                }, executor);
            }

            @Override
            public CompletableFuture<Void> apply(List<NbtCompound> data, ResourceManager manager, Profiler profiler, Executor executor) {
                return CompletableFuture.runAsync(() -> {
                    ArachneClient.latestReload = System.nanoTime();
                    ArachneTabResources.needsReload = true;
                    ArachneTabResources.entryList.clear();
                    ArachneTabResources.entryList.addAll(data);
                }, executor);
            }
        });
    }

    // last pack reload time
    public static long latestReload = Long.MIN_VALUE;

    public static WorldRenderEvents.BeforeBlockOutline listener() {
        return (context, hitResult) -> !(MinecraftClient.getInstance().currentScreen instanceof EditorMainScreen);
    }
}