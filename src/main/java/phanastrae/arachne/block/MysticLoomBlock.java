package phanastrae.arachne.block;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.screen.LoomScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import phanastrae.arachne.networking.screen_handler.MysticLoomScreenHandler;

public class MysticLoomBlock extends HorizontalFacingBlock {
    private static final Text TITLE = Text.translatable("container.arachne.mystic_loom");

    protected final VoxelShape[] shapes;

    private final Object2IntMap<BlockState> SHAPE_INDEX_CACHE = new Object2IntOpenHashMap<BlockState>();

    public MysticLoomBlock(Settings settings) {
        super(settings);
        this.shapes = this.createShapes();
    }

    protected VoxelShape[] createShapes() {
        VoxelShape base  = Block.createCuboidShape( 0,  0, 0, 16, 14, 16);
        VoxelShape northBack = Block.createCuboidShape( 1, 16,13, 15, 30, 15);
        VoxelShape eastBack  = Block.createCuboidShape( 1, 16, 1,  3, 30, 15);
        VoxelShape southBack = Block.createCuboidShape( 1, 16, 1, 15, 30,  3);
        VoxelShape westBack  = Block.createCuboidShape(13, 16, 1, 15, 30, 15);
        VoxelShape northLong = Block.createCuboidShape( 0, 14,13, 16, 16, 16);
        VoxelShape eastLong  = Block.createCuboidShape( 0, 14, 0,  3, 16, 16);
        VoxelShape southLong = Block.createCuboidShape( 0, 14, 0, 16, 16,  3);
        VoxelShape westLong  = Block.createCuboidShape(13, 14, 0,16, 16, 16);
        VoxelShape NSLong = VoxelShapes.union(northLong, southLong);
        VoxelShape EWLong = VoxelShapes.union(eastLong, westLong);
        northBack = VoxelShapes.union(northBack, EWLong);
        eastBack = VoxelShapes.union(eastBack, NSLong);
        southBack = VoxelShapes.union(southBack, EWLong);
        westBack = VoxelShapes.union(westBack, NSLong);
        VoxelShape northShort = Block.createCuboidShape( 3, 14,12, 13, 16, 16);
        VoxelShape eastShort  = Block.createCuboidShape( 0, 14, 3,  4, 16, 13);
        VoxelShape southShort = Block.createCuboidShape( 3, 14, 0, 13, 16,  4);
        VoxelShape westShort  = Block.createCuboidShape(12, 14, 3,16, 16, 13);
        northBack = VoxelShapes.union(northBack, northShort);
        eastBack = VoxelShapes.union(eastBack, eastShort);
        southBack = VoxelShapes.union(southBack, southShort);
        westBack = VoxelShapes.union(westBack, westShort);
        VoxelShape[] voxelShapes = new VoxelShape[]{northBack, eastBack, southBack, westBack};
        for (int j = 0; j < 4; ++j) {
            voxelShapes[j] = VoxelShapes.union(base, voxelShapes[j]);
        }
        return voxelShapes;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        }
        player.openHandledScreen(state.createScreenHandlerFactory(world, pos));
        player.incrementStat(Stats.INTERACT_WITH_LOOM);
        return ActionResult.CONSUME;
    }

    @Nullable
    @Override
    public NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
        return new SimpleNamedScreenHandlerFactory((syncId, inventory, player) -> new MysticLoomScreenHandler(syncId, inventory, ScreenHandlerContext.create(world, pos)), TITLE);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return this.shapes[this.getShapeIndex(state)];
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return this.shapes[this.getShapeIndex(state)];
    }

    protected int getShapeIndex(BlockState state2) {
        return this.SHAPE_INDEX_CACHE.computeIntIfAbsent(state2, state -> {
            if (state.get(FACING).equals(Direction.NORTH)) {
                return 0;
            }
            if (state.get(FACING).equals(Direction.EAST)) {
                return 1;
            }
            if (state.get(FACING).equals(Direction.SOUTH)) {
                return 2;
            }
            if (state.get(FACING).equals(Direction.WEST)) {
                return 3;
            }
            return 0;
        });
    }
}
