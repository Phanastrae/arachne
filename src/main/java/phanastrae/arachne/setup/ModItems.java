package phanastrae.arachne.setup;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import phanastrae.arachne.Arachne;
import phanastrae.arachne.item.SketchItem;
import phanastrae.arachne.item.SketchingTableItem;
import phanastrae.arachne.item.WeaveControlItem;
import phanastrae.arachne.item.WeaveItem;

import java.util.ArrayList;

public class ModItems {

    public static final Item SKETCH = new SketchItem(new FabricItemSettings());
    public static final Item FILLED_SKETCH = new SketchItem(new FabricItemSettings().maxCount(1));
    public static final Item WEAVE = new WeaveItem(new FabricItemSettings());
    public static final Item SKETCHING_TABLE = new SketchingTableItem(ModBlocks.SKETCHING_TABLE, new FabricItemSettings());
    public static final Item MYSTIC_LOOM = new BlockItem(ModBlocks.MYSTIC_LOOM, new FabricItemSettings());
    public static final Item WEAVE_CONTROLLER = new WeaveControlItem(new FabricItemSettings().maxCount(1));

    public static final ItemGroup ARACHNE_GROUP = FabricItemGroup.builder()
            .icon(() -> new ItemStack(WEAVE))
            .displayName(Text.translatable("itemGroup.arachne.group"))
            .build();

    public static void init() {
        registerPlusMenu(SKETCH, "sketch");
        register(FILLED_SKETCH, "filled_sketch");
        register(WEAVE, "weave");
        registerPlusMenu(SKETCHING_TABLE, "sketching_table");
        registerPlusMenu(MYSTIC_LOOM, "mystic_loom");
        registerPlusMenu(WEAVE_CONTROLLER, "weave_controller");

        Registry.register(Registries.ITEM_GROUP, Arachne.id("arachne"), ARACHNE_GROUP);
    }

    public static <T extends Item> void registerPlusMenu(T item, String name) {
        register(item, name);
        addToMenu(item);
    }

    public static <T extends Item> void addToMenu(T item) {
        ItemGroupEvents.modifyEntriesEvent(RegistryKey.of(Registries.ITEM_GROUP.getKey(), Arachne.id("arachne"))).register(entries -> entries.add(item));
    }

    public static <T extends Item> void register(T item, String name) {
        Registry.register(Registries.ITEM, Arachne.id(name), item);
    }
}
