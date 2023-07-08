package phanastrae.arachne.mixin.client;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemGroups.class)
public interface ItemGroupsAccessor {
    @Accessor
    static ItemGroup.DisplayContext getDisplayContext() {
        throw new AssertionError();
    }
}
