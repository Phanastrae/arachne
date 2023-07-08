package phanastrae.arachne.screen;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import phanastrae.arachne.Arachne;
import phanastrae.arachne.mixin.client.ItemGroupsAccessor;
import phanastrae.arachne.setup.ModItems;

import java.util.ArrayList;
import java.util.List;

public class ArachneTabResources {

    public static boolean needsReload = false;

    public static List<NbtCompound> entryList = new ArrayList<>();

    public static void updateGroupIfNeeded() {
        if(needsReload) {
            needsReload = false;
            ItemGroup.DisplayContext displayContext = ItemGroupsAccessor.getDisplayContext();
            ModItems.ARACHNE_GROUP.updateEntries(displayContext);
        }
    }


    // this gets called every time the creative menu is opened after a resource reload (including on launch)
    public static void addPrefabsToMenu(FabricItemGroupEntries entries) {
        for(NbtCompound nbt : entryList) {
            String id = nbt.getString("fileId");
            nbt.remove("fileId");

            ItemStack sketchStack = new ItemStack(ModItems.FILLED_SKETCH);
            ItemStack weaveStack = new ItemStack(ModItems.WEAVE);
            sketchStack.getOrCreateNbt().put("sketchData", nbt);
            weaveStack.getOrCreateNbt().put("weaveData", nbt.copy());
            sketchStack.setCustomName(Text.of("Sketch: " + id));
            weaveStack.setCustomName(Text.of("Weave: " + id));
            entries.add(sketchStack);
            entries.add(weaveStack);
        }
    }
}
