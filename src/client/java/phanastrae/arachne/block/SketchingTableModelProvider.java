package phanastrae.arachne.block;

import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelProviderException;
import net.fabricmc.fabric.api.client.model.ModelResourceProvider;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import phanastrae.arachne.Arachne;

public class SketchingTableModelProvider implements ModelResourceProvider {
    public static final Identifier SKETCHING_TABLE_MODEL_BLOCK = Arachne.id("block/sketching_table");
    public static final Identifier SKETCHING_TABLE_MODEL_ITEM = Arachne.id("item/sketching_table");
    @Override
    public @Nullable UnbakedModel loadModelResource(Identifier resourceId, ModelProviderContext context) throws ModelProviderException {
        if(resourceId.equals(SKETCHING_TABLE_MODEL_BLOCK) || resourceId.equals(SKETCHING_TABLE_MODEL_ITEM)) {
            return new SketchingTableModel();
        } else {
            return null;
        }
    }
}
