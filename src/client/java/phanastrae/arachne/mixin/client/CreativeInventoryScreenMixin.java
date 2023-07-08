package phanastrae.arachne.mixin.client;

import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.featuretoggle.FeatureSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import phanastrae.arachne.screen.ArachneTabResources;

@Mixin(CreativeInventoryScreen.class)
public class CreativeInventoryScreenMixin {

    @Inject(method = "updateDisplayParameters", at = @At("RETURN"))
    public void arachne_updateGroup(FeatureSet enabledFeatures, boolean showOperatorTab, RegistryWrapper.WrapperLookup lookup, CallbackInfo ci) {
        ArachneTabResources.updateGroupIfNeeded();
    }
}
