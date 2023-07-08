package phanastrae.arachne.setup;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import phanastrae.arachne.networking.screen_handler.MysticLoomScreenHandler;
import phanastrae.arachne.networking.screen_handler.SketchingTableScreenHandler;

public class ModScreenHandlerTypes {
    public static final ScreenHandlerType<SketchingTableScreenHandler> SKETCHING_TABLE = register("sketching_table", SketchingTableScreenHandler::new);
    public static final ScreenHandlerType<MysticLoomScreenHandler> MYSTIC_LOOM = register("mystic_loom", MysticLoomScreenHandler::new);

    public static void init() {
    }

    private static <T extends ScreenHandler> ScreenHandlerType<T> register(String id, ScreenHandlerType.Factory<T> factory) {
        return Registry.register(Registries.SCREEN_HANDLER, id, new ScreenHandlerType<T>(factory, FeatureFlags.VANILLA_FEATURES)); // TODO: is this the right flag??
    }
}
