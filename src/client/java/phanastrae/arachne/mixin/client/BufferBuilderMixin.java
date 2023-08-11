package phanastrae.arachne.mixin.client;

import net.minecraft.client.render.BufferBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import phanastrae.arachne.render.BufferBuilderAccess;

@Mixin(BufferBuilder.class)
public abstract class BufferBuilderMixin implements BufferBuilderAccess {

    @Shadow protected abstract void grow(int size);

    public void doGrow(int i) {
        grow(i);
    }
}
