package phanastrae.arachne.networking;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import phanastrae.arachne.Arachne;
import phanastrae.arachne.block.blockentity.SketchingTableBlockEntity;
import phanastrae.arachne.setup.ModItems;
import phanastrae.arachne.networking.screen_handler.SketchingTableScreenHandler;
import phanastrae.arachne.weave.NBTSerialization;
import phanastrae.arachne.weave.SketchWeave;
import phanastrae.old.Weave;

import java.io.*;

public class SketchUpdateC2SPacket implements FabricPacket {
    public static final PacketType<SketchUpdateC2SPacket> TYPE = PacketType.create(PacketIds.SKETCH_UPDATE_PACKET_ID, SketchUpdateC2SPacket::new);
    // TODO consider some sort of rate limit and/or greater filtering
    // TODO might need a size limit

    private final SketchWeave sketchWeave;
    private final int x;
    private final int y;
    private final int z;

    public SketchUpdateC2SPacket(SketchWeave weave, int x, int y, int z) {
        this.sketchWeave = weave;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public SketchUpdateC2SPacket(PacketByteBuf buf) {
        ByteArrayInputStream BAIS = new ByteArrayInputStream(buf.readByteArray());
        this.x = buf.readInt();
        this.y = buf.readInt();
        this.z = buf.readInt();
        NbtCompound nbt = null;
        try {
            nbt = NbtIo.readCompressed(BAIS);
        } catch(IOException ignored) {
        }
        if(nbt == null) {
            this.sketchWeave = null;
        } else {
            this.sketchWeave = NBTSerialization.readSketchWeave(nbt);
        }
    }

    public void write(PacketByteBuf buf) {
        NbtCompound nbt = getNbt();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            if(nbt != null) {
                NbtIo.writeCompressed(nbt, baos);
            }
        } catch (IOException ignored) {
        }
        buf.writeByteArray(baos.toByteArray());
        buf.writeInt(this.x);
        buf.writeInt(this.y);
        buf.writeInt(this.z);
    }

    public void execute(MinecraftServer server, ServerPlayerEntity serverPlayerEntity) {
        Arachne.LOGGER.info("Recieved Sketch Data from " + serverPlayerEntity.getName().getString());
        World world = serverPlayerEntity.getWorld();
        if(world == null) return;

        // TODO make position check more strict?
        if(serverPlayerEntity.getPos().subtract(this.x, this.y, this.z).length() > 16) return;
        BlockPos pos = new BlockPos(this.x, this.y, this.z);

        BlockEntity be = world.getBlockEntity(pos);
        if(be == null) return;

        if(!(be instanceof SketchingTableBlockEntity stbe)) return;

        ItemStack itemStack = stbe.getStack(0);
        if(itemStack == null || !(itemStack.isOf(ModItems.SKETCH) || itemStack.isOf(ModItems.FILLED_SKETCH))) return;
        NbtCompound weaveNbt = this.getNbt();
        if(weaveNbt == null) weaveNbt = new NbtCompound();
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
        stbe.setStack(0, newStack);
    }

    public NbtCompound getNbt() {
        return sketchWeave == null ? null : NBTSerialization.writeSketchWeave(sketchWeave);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
