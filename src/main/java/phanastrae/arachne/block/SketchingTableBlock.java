package phanastrae.arachne.block;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.screen.*;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import phanastrae.arachne.setup.ModBlockEntities;
import phanastrae.arachne.block.blockentity.SketchingTableBlockEntity;
import phanastrae.arachne.networking.screen_handler.SketchingTableScreenHandler;
import phanastrae.arachne.setup.ModItems;
import phanastrae.arachne.util.TableMultiblock;

public class SketchingTableBlock extends BlockWithEntity {
    public static final BooleanProperty MARKED = BooleanProperty.of("marked");

    public SketchingTableBlock(Settings settings) {
        super(settings);
        this.setDefaultState((BlockState)this.getDefaultState().with(MARKED, false));
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack itemStack = player.getStackInHand(hand);
        BlockEntity blockEntity = world.getBlockEntity(pos);
        // allow placing of tables on sides of other tables
        if(itemStack.isOf(ModItems.SKETCHING_TABLE) && hit.getSide() != null && !hit.getSide().getAxis().equals(Direction.Axis.Y)) {
            return ActionResult.PASS;
        }

        if(player.isSneaking()) {
            if (blockEntity instanceof SketchingTableBlockEntity sketchingTableBlockEntity && !sketchingTableBlockEntity.isEmpty()) {
                if(!world.isClient) {
                    sketchingTableBlockEntity.dropItems(player);
                    return ActionResult.SUCCESS;
                }
                return ActionResult.CONSUME;
            }

            BlockState blockState = state.cycle(MARKED);
            if (world.isClient) {
                if (!blockState.get(MARKED)) {
                    SketchingTableBlock.spawnParticles(blockState, world, pos, 1.0f);
                }
                return ActionResult.SUCCESS;
            }
            world.setBlockState(pos, blockState, Block.NOTIFY_LISTENERS);
            // TODO: change sound?
            world.playSound(null, pos, SoundEvents.ENTITY_VILLAGER_WORK_CARTOGRAPHER, SoundCategory.BLOCKS, 0.3f, blockState.get(MARKED) ? 1.2f : 0.8f);
            world.emitGameEvent((Entity) player, blockState.get(MARKED) ? GameEvent.BLOCK_ACTIVATE : GameEvent.BLOCK_DEACTIVATE, pos);
            return ActionResult.CONSUME;
        } else {
            if (blockEntity instanceof SketchingTableBlockEntity sketchingTableBlockEntity && sketchingTableBlockEntity.canAccept(itemStack)) {
                if (!world.isClient && sketchingTableBlockEntity.addItem(player, player.getAbilities().creativeMode ? itemStack.copy() : itemStack)) {
                    //player.incrementStat(Stats.INTERACT_WITH_CAMPFIRE);
                    world.playSound(null, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 0.3f, 1f);
                    return ActionResult.SUCCESS;
                }
            }

            if (world.isClient) {
                return ActionResult.SUCCESS;
            } else {
                player.openHandledScreen(state.createScreenHandlerFactory(world, pos));
                return ActionResult.CONSUME;
            }
        }
    }

    @Override
    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState();
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(MARKED);
    }

    private static void spawnParticles(BlockState state, WorldAccess world, BlockPos pos, float alpha) {
        double d = (double)pos.getX() + 0.5;
        double e = (double)pos.getY() + 1;
        double f = (double)pos.getZ() + 0.5;
        world.addParticle(new DustParticleEffect(new Vector3f(1, 1, 1), alpha), d, e, f, 0.0, 0.0, 0.0);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new SketchingTableBlockEntity(pos, state);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.isOf(newState.getBlock())) {
            return;
        }
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof SketchingTableBlockEntity) {
            ItemScatterer.spawn(world, pos, ((SketchingTableBlockEntity)blockEntity).getItems());
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Nullable
    @Override
    public NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
        BlockEntity be = world.getBlockEntity(pos);
        Inventory inventory = null;
        if(be instanceof SketchingTableBlockEntity stbe) {
            inventory = stbe;
        }
        if(inventory == null) {
            inventory = new SimpleInventory(1);
        }
        Inventory finalInventory = inventory;
        return new SimpleNamedScreenHandlerFactory((syncId, playerInventory, player) -> {
            // try to select targeted marker if it exists
            BlockPos bp = null;
            HitResult hitResult = player.raycast(16, 0, false);
            if(hitResult != null) {
                TableMultiblock tableMultiblock = new TableMultiblock(pos);
                tableMultiblock.init(world);
                bp = tableMultiblock.getNearestMarker(hitResult.getPos());
            }
            if(bp == null) {
                bp = pos;
            }

            PropertyDelegate pd = new ArrayPropertyDelegate(3);
            pd.set(0, bp.getX());
            pd.set(1, bp.getY());
            pd.set(2, bp.getZ());
            return new SketchingTableScreenHandler(syncId, playerInventory, finalInventory, ScreenHandlerContext.create(world, bp), pd);
        }, Text.literal("TEMP TEXT, IGNORE")); // TODO
    }

    @Override
    public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
        BlockEntity be = world.getBlockEntity(pos);
        if(be instanceof SketchingTableBlockEntity stbe) {
            Inventory inventory = stbe;
            ItemStack itemStack = inventory.getStack(0);
            if(itemStack != null && !itemStack.isEmpty()) {
                return itemStack.copyWithCount(1);
            }
        }
        return super.getPickStack(world, pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return world.isClient ? SketchingTableBlock.checkType(type, ModBlockEntities.SKETCHING_TABLE, SketchingTableBlockEntity::tick) : null;
    }
}
