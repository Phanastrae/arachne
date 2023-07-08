package phanastrae.arachne.item;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import phanastrae.arachne.weave.WeaveCache;
import phanastrae.arachne.weave.WeavePreviewRenderer;
import phanastrae.arachne.weave.Weave;

public class SketchTooltipComponent implements TooltipComponent {

    public static WeaveCache WEAVE_LAST = new WeaveCache();

    @Nullable
    private final Weave weave;

    public SketchTooltipComponent(SketchTooltipData data) {
        NbtCompound nbtCompound;
        switch (data.getWeaveDataType()) {
            case SKETCH -> nbtCompound = WeaveCache.getNbtSketch(data.getNbt());
            case WEAVE -> nbtCompound = WeaveCache.getNbtWeave(data.getNbt());
            default -> nbtCompound = null;
        }
        this.weave = WEAVE_LAST.getOrMakeWeave(nbtCompound, Weave::new);
    }

    public int PADDING = 3;
    public int WIDTH = 128;
    public int HEIGHT = 128;

    @Override
    public int getHeight() {
        return this.weave == null ? 0 : (HEIGHT + PADDING * 2);
    }

    @Override
    public int getWidth(TextRenderer textRenderer) {
        return this.weave == null ? 0 : (WIDTH + PADDING * 2);
    }

    @Override
    public void drawText(TextRenderer textRenderer, int x, int y, Matrix4f matrix, VertexConsumerProvider.Immediate vertexConsumers) {
    }

    @Override
    public void drawItems(TextRenderer textRenderer, int x, int y, DrawContext context) {
        WeavePreviewRenderer.render(weave, context, x + PADDING, y + PADDING, WIDTH, HEIGHT, 0xFFFFFFFF);
    }
}
