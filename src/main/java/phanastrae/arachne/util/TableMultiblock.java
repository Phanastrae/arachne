package phanastrae.arachne.util;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import phanastrae.arachne.setup.ModBlocks;
import phanastrae.arachne.block.SketchingTableBlock;

import java.util.ArrayList;
import java.util.List;

public class TableMultiblock {

    public static final int DEFAULT_RANGE = 7;

    final BlockPos center;
    final int radius;
    BlockType[][] blockArray;

    public TableMultiblock(BlockPos center) {
        this(center, DEFAULT_RANGE);
    }

    public TableMultiblock(BlockPos center, int radius) {
        this.center = center;
        this.radius = radius;
        int SIZE = 1 + 2 * radius;
        this.blockArray = new BlockType[SIZE][SIZE];
    }

    public enum BlockType {
        OUT_OF_BOUNDS,
        OTHER,
        TABLE,
        MARKED
    }

    public void init(World world) {
        int SIZE = 1 + 2 * radius;
        this.blockArray = new BlockType[SIZE][SIZE];
        List<BlockPos> queue = new ArrayList<>();
        if(setFromState(center, world.getBlockState(center))) {
            queue.add(this.center);
        } else {
            return;
        }
        int MAX_ITERATIONS = SIZE*SIZE;
        for(int i = 0; i < MAX_ITERATIONS && !queue.isEmpty(); i++) {
            List<BlockPos> nextQueue = new ArrayList<>();
            for(BlockPos pos : queue) {
                for(Direction d : Direction.Type.HORIZONTAL) {
                    BlockPos next = pos.add(d.getVector());
                    if(getValue(next) != null) continue;
                    if(setFromState(next, world.getBlockState(next))) {
                        nextQueue.add(next);
                    }
                }
            }
            queue = nextQueue;
        }
    }

    public boolean setFromState(BlockPos pos, BlockState state) {
        if(state.isOf(ModBlocks.SKETCHING_TABLE)) {
            if(state.get(SketchingTableBlock.MARKED)) {
                setValue(pos, BlockType.MARKED);
            } else {
                setValue(pos, BlockType.TABLE);
            }
            return true;
        } else {
            setValue(pos, BlockType.OTHER);
            return false;
        }
    }

    public void setValue(BlockPos pos, BlockType blockType) {
        if(pos.getY() != center.getY()) return;

        int i = pos.getX() - center.getX() + this.radius;
        if(i < 0 || this.blockArray.length <= i) return;
        int j = pos.getZ() - center.getZ() + this.radius;
        if(j < 0 || this.blockArray[i].length <= j) return;

        this.blockArray[i][j] = blockType;
    }

    public BlockType getValue(BlockPos pos) {
        if(pos.getY() != center.getY()) return BlockType.OUT_OF_BOUNDS;

        int i = pos.getX() - center.getX() + this.radius;
        if(i < 0 || this.blockArray.length <= i) return BlockType.OUT_OF_BOUNDS;
        int j = pos.getZ() - center.getZ() + this.radius;
        if(j < 0 || this.blockArray[i].length <= j) return BlockType.OUT_OF_BOUNDS;

        return this.blockArray[i][j];
    }

    public BlockPos getNearestMarker(Vec3d pos) {
        if(this.blockArray == null) return null;

        Vec3d posRelative = pos.subtract(this.center.toCenterPos()).add(this.radius, 0, this.radius);
        double minDistSqr = Double.MAX_VALUE;
        BlockPos nearestMark = null;
        for(int i = 0; i < this.blockArray.length; i++) {
            for(int j = 0; j < this.blockArray[i].length; j++) {
                BlockType blockType = this.blockArray[i][j];
                if(blockType == BlockType.MARKED) {
                    Vec3d markPos = new Vec3d(i, 0, j);
                    double distanceSquared = posRelative.subtract(markPos).lengthSquared();
                    if(distanceSquared < minDistSqr) {
                        minDistSqr = distanceSquared;
                        nearestMark = new BlockPos(center.getX() - this.radius + i, center.getY(), center.getZ() - this.radius + j);
                    }
                }
            }
        }
        return nearestMark;
    }

    public List<BlockPos> getBlocks(BlockType type) {
        if(this.blockArray == null) return null;

        List<BlockPos> blocks = new ArrayList<>();
        for(int i = 0; i < this.blockArray.length; i++) {
            for(int j = 0; j < this.blockArray[i].length; j++) {
                BlockPos blockPos = new BlockPos(this.center.getX() - this.radius + i, this.center.getY(), this.center.getZ() - this.radius + j);
                BlockType blockType = this.blockArray[i][j];
                if(blockType == type) {
                    blocks.add(blockPos);
                }
            }
        }
        return blocks;
    }

    public BlockPos getCenter() {
        return this.center;
    }

    public boolean isCenter(BlockPos pos) {
        return pos.equals(center);
    }

    public static boolean isTypeInTable(BlockType type) {
        return type != null && type != BlockType.OUT_OF_BOUNDS && type != BlockType.OTHER;
    }
}
