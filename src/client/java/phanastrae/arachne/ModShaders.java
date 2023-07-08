package phanastrae.arachne;

import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormats;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class ModShaders {
    @Nullable
    private static ShaderProgram diskShader;
    public static String diskShaderID = "disk";
    @Nullable
    public static ShaderProgram getDiskShader() {
        return diskShader;
    }
    protected static final RenderPhase.ShaderProgram DISK_SHADER = new RenderPhase.ShaderProgram(ModShaders::getDiskShader);

    public static void registerShaders(CoreShaderRegistrationCallback.RegistrationContext registrationContext) throws IOException {
        // TODO: handle IOexception?
        registrationContext.register(Arachne.id(diskShaderID), VertexFormats.POSITION_COLOR_TEXTURE, shaderProgram -> ModShaders.diskShader = shaderProgram);
    }
}
