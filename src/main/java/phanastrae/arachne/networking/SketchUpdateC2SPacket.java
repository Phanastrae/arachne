package phanastrae.arachne.networking;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import phanastrae.arachne.setup.ModItems;
import phanastrae.arachne.networking.screen_handler.SketchingTableScreenHandler;
import phanastrae.arachne.weave.Weave;

import java.io.*;

public class SketchUpdateC2SPacket implements FabricPacket {
    public static final PacketType<SketchUpdateC2SPacket> TYPE = PacketType.create(PacketIds.SKETCH_UPDATE_PACKET_ID, SketchUpdateC2SPacket::new);
    // TODO consider some sort of rate limit and/or greater filtering
    // TODO might need a size limit

    private final Weave weave;

    public SketchUpdateC2SPacket(Weave weave) {
        this.weave = weave;
    }

    public SketchUpdateC2SPacket(PacketByteBuf buf) {
        Weave weave1;
        try {
            NbtCompound nbt = NbtIo.readCompressed(new ByteArrayInputStream(buf.readByteArray()));
            weave1 = new Weave();
            if(nbt != null) {
                weave1.readFromNBT(nbt);
            }
        } catch(IOException ignored) {
            weave1 = null;
        }
        this.weave = weave1;
    }

    public void write(PacketByteBuf buf) {
        NbtCompound nbt = new NbtCompound();
        weave.writeToNBT(nbt);
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            NbtIo.writeCompressed(nbt, baos);
            buf.writeByteArray(baos.toByteArray());
        } catch (IOException ignored) {
        }
    }

    public void execute(MinecraftServer server, ServerPlayerEntity serverPlayerEntity) {
        World world = serverPlayerEntity.getWorld();
        if(world == null) return;
        ScreenHandler screenHandler = serverPlayerEntity.currentScreenHandler;
        if(screenHandler instanceof SketchingTableScreenHandler stsh) {
            ItemStack itemStack = stsh.getInventory().getStack(0);
            if(itemStack == null || !(itemStack.isOf(ModItems.SKETCH) || itemStack.isOf(ModItems.FILLED_SKETCH))) return;
            NbtCompound weaveNbt = this.getNbt();
            boolean empty = weaveNbt.isEmpty();
            Item item = empty ? ModItems.SKETCH : ModItems.FILLED_SKETCH;

            ItemStack newStack = new ItemStack(item, itemStack.getCount());
            NbtCompound nbt;
            if(itemStack.getNbt() != null) {
                nbt = itemStack.getNbt().copy();
            } else {
                nbt = new NbtCompound();
            }
            nbt.remove("sketchData");
            if(!weaveNbt.isEmpty()) {
                nbt.put("sketchData", weaveNbt);
            }
            if(!nbt.isEmpty()) {
                newStack.setNbt(nbt);
            }
            stsh.getInventory().setStack(0, newStack);
        }
    }

    public Weave getWeave() {
        return this.weave;
    }

    public NbtCompound getNbt() {
        NbtCompound nbt = new NbtCompound();
        this.weave.writeToNBT(nbt);
        return nbt;
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
