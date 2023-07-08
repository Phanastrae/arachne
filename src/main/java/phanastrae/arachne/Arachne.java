package phanastrae.arachne;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import phanastrae.arachne.setup.ModBlocks;
import phanastrae.arachne.setup.ModBlockEntities;
import phanastrae.arachne.setup.ModEntities;
import phanastrae.arachne.setup.ModScreenHandlerTypes;
import phanastrae.arachne.setup.ModItems;
import phanastrae.arachne.networking.SketchUpdateC2SPacket;

public class Arachne implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("Arachne");

    public static Identifier id (String str) {
        return new Identifier("arachne", str);
    }

    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.
        ModBlocks.init();
        ModItems.init();
        ModBlockEntities.init();
        ModEntities.init();
        ModScreenHandlerTypes.init();

        ServerPlayNetworking.registerGlobalReceiver(SketchUpdateC2SPacket.TYPE, (packet, player, packetSender) -> {
            MinecraftServer server = player.getServer();
            if(server == null) return;
            player.getServer().execute(() -> packet.execute(server, player));
        });
    }
}