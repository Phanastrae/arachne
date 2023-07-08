package phanastrae.arachne.block;

import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.ModelHelper;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.*;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;
import phanastrae.arachne.setup.ModBlocks;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static net.minecraft.screen.PlayerScreenHandler.BLOCK_ATLAS_TEXTURE;

public class SketchingTableModel implements UnbakedModel, BakedModel, FabricBakedModel {

    private static final SpriteIdentifier[] SPRITE_IDS = new SpriteIdentifier[]{
            new SpriteIdentifier(BLOCK_ATLAS_TEXTURE, new Identifier("arachne:block/sketching_table_top")),
            new SpriteIdentifier(BLOCK_ATLAS_TEXTURE, new Identifier("arachne:block/sketching_table_top_marked")),
            new SpriteIdentifier(BLOCK_ATLAS_TEXTURE, new Identifier("arachne:block/sketching_table_top_frame")),
            new SpriteIdentifier(BLOCK_ATLAS_TEXTURE, new Identifier("arachne:block/sketching_table_top_frame_alt")),
            new SpriteIdentifier(BLOCK_ATLAS_TEXTURE, new Identifier("arachne:block/sketching_table_side")),
            new SpriteIdentifier(BLOCK_ATLAS_TEXTURE, new Identifier("arachne:block/sketching_table_side_frame")),
            new SpriteIdentifier(BLOCK_ATLAS_TEXTURE, new Identifier("arachne:block/sketching_table_bottom"))
    };
    private Sprite[] SPRITES = new Sprite[SPRITE_IDS.length];

    @Override
    public Collection<Identifier> getModelDependencies() {
        return Collections.emptyList();
    }

    @Override
    public void setParents(Function<Identifier, UnbakedModel> modelLoader) {
         // TODO
    }

    Mesh ITEM;
    Mesh CORE;
    Mesh CORE_MARKED;
    Mesh[] NORTH = new Mesh[2];
    Mesh[] SOUTH = new Mesh[2];
    Mesh[] EAST = new Mesh[2];
    Mesh[] WEST = new Mesh[2];
    Mesh[] NE = new Mesh[3];
    Mesh[] NW = new Mesh[3];
    Mesh[] SE = new Mesh[3];
    Mesh[] SW = new Mesh[3];

    @Nullable
    @Override
    public BakedModel bake(Baker baker, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
        for(int i = 0; i < SPRITE_IDS.length; i++) {
            this.SPRITES[i] = textureGetter.apply(SPRITE_IDS[i]);
        }

        Renderer renderer = RendererAccess.INSTANCE.getRenderer();
        MeshBuilder builder = renderer.meshBuilder();
        QuadEmitter emitter = builder.getEmitter();

        emitter.square(Direction.UP, 1/8f, 1/8f, 7/8f, 7/8f, 0);
        emitter.spriteBake(0, SPRITES[0], MutableQuadView.BAKE_LOCK_UV);
        emitter.spriteColor(0, -1, -1, -1, -1);
        emitter.emit();
        emitter.square(Direction.DOWN, 0, 0, 1, 1, 0);
        emitter.spriteBake(0, SPRITES[6], MutableQuadView.BAKE_LOCK_UV);
        emitter.spriteColor(0, -1, -1, -1, -1);
        emitter.emit();
        this.CORE = builder.build();

        emitter.square(Direction.UP, 1/8f, 1/8f, 7/8f, 7/8f, 0);
        emitter.spriteBake(0, SPRITES[1], MutableQuadView.BAKE_LOCK_UV);
        emitter.spriteColor(0, -1, -1, -1, -1);
        emitter.emit();
        emitter.square(Direction.DOWN, 0, 0, 1, 1, 0);
        emitter.spriteBake(0, SPRITES[6], MutableQuadView.BAKE_LOCK_UV);
        emitter.spriteColor(0, -1, -1, -1, -1);
        emitter.emit();
        this.CORE_MARKED = builder.build();

        this.NORTH = buildSide(builder, Direction.NORTH);
        this.SOUTH = buildSide(builder, Direction.SOUTH);
        this.EAST = buildSide(builder, Direction.EAST);
        this.WEST = buildSide(builder, Direction.WEST);
        this.NE = buildCorner(builder, Direction.NORTH, Direction.EAST);
        this.NW = buildCorner(builder, Direction.NORTH, Direction.WEST);
        this.SE = buildCorner(builder, Direction.SOUTH, Direction.EAST);
        this.SW = buildCorner(builder, Direction.SOUTH, Direction.WEST);

        for(Direction direction : Direction.values()) { // TODO: make right
            int sprite = 5;
            if(direction == Direction.UP) {
                sprite = 3;
            } else if(direction == Direction.DOWN) {
                sprite = 6;
            }
            emitter.square(direction, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f);
            emitter.spriteBake(0, SPRITES[sprite], MutableQuadView.BAKE_LOCK_UV);
            emitter.spriteColor(0, -1, -1, -1, -1);
            emitter.emit();
        }
        this.ITEM = builder.build();
        return this;
    }

    public Mesh[] buildCorner(MeshBuilder builder, Direction directionNS, Direction directionEW) {
        Mesh[] meshes = new Mesh[3];
        meshes[0] = bakeCorner(builder, directionNS, directionEW, 0);
        meshes[1] = bakeCorner(builder, directionNS, directionEW, 2);
        meshes[2] = bakeCorner(builder, directionNS, directionEW, 3);
        return meshes;
    }

    public Mesh bakeCorner(MeshBuilder builder, Direction directionNS, Direction directionEW, int texTop) {
        QuadEmitter emitter = builder.getEmitter();
        float left = 0;
        float right = 1;
        float bottom = 0;
        float top = 1;
        if(directionNS == Direction.NORTH) {
            bottom = 7/8f;
        } else if(directionNS == Direction.SOUTH) {
            top = 1/8f;
        }
        if(directionEW == Direction.EAST) {
            left = 7/8f;
        } else if(directionEW == Direction.WEST) {
            right = 1/8f;
        }
        emitter.square(Direction.UP, left, bottom, right, top, 0);
        emitter.spriteBake(0, SPRITES[texTop], MutableQuadView.BAKE_LOCK_UV);
        emitter.spriteColor(0, -1, -1, -1, -1);
        emitter.emit();
        return builder.build();
    }

    public Mesh[] buildSide(MeshBuilder builder, Direction direction) {
        Mesh[] meshes = new Mesh[2];
        meshes[0] = bakeConnection(builder, direction, 4, 0);
        meshes[1] = bakeConnection(builder, direction, 5, 2);
        return meshes;
    }

    public Mesh bakeConnection(MeshBuilder builder, Direction direction, int texSide, int texTop) { // TODO: tidy
        QuadEmitter emitter = builder.getEmitter();
        emitter.square(direction.rotateClockwise(Direction.Axis.Y), 0.5f, 0, 1, 1, 0);
        emitter.spriteBake(0, SPRITES[texSide], MutableQuadView.BAKE_LOCK_UV);
        emitter.spriteColor(0, -1, -1, -1, -1);
        emitter.emit();
        emitter.square(direction.rotateCounterclockwise(Direction.Axis.Y), 0, 0, 0.5f, 1, 0);
        emitter.spriteBake(0, SPRITES[texSide], MutableQuadView.BAKE_LOCK_UV);
        emitter.spriteColor(0, -1, -1, -1, -1);
        emitter.emit();
        float left = 1/8f;
        float right = 7/8f;
        float bottom = 1/8f;
        float top = 7/8f;
        if(direction == Direction.NORTH) {
            bottom = 7/8f;
            top = 1;
        } else if(direction == Direction.SOUTH) {
            bottom = 0;
            top = 1/8f;
        } else if(direction == Direction.EAST) {
            left = 7/8f;
            right = 1;
        } else if(direction == Direction.WEST) {
            left = 0;
            right = 1/8f;
        }
        emitter.square(Direction.UP, left, bottom, right, top, 0);
        emitter.spriteBake(0, SPRITES[texTop], MutableQuadView.BAKE_LOCK_UV);
        emitter.spriteColor(0, -1, -1, -1, -1);
        emitter.emit();
        return builder.build();
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, Random random) {
        return Collections.emptyList();
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isBuiltin() {
        return false;
    }

    @Override
    public boolean hasDepth() {
        return false;
    }

    @Override
    public boolean isSideLit() {
        return true;
    }

    @Override
    public Sprite getParticleSprite() {
        return SPRITES[0]; // TODO
    }

    @Override
    public ModelTransformation getTransformation() {
        return ModelHelper.MODEL_TRANSFORM_BLOCK;
    }

    @Override
    public ModelOverrideList getOverrides() {
        return ModelOverrideList.EMPTY;
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
        // TODO
        // TODO: check this doesn't cause any issues
        //context.meshConsumer().accept(mesh);
        boolean marked = state.get(SketchingTableBlock.MARKED);
        boolean north = isSketchTable(blockView, pos.north());
        boolean south = isSketchTable(blockView, pos.south());
        boolean east = isSketchTable(blockView, pos.east());
        boolean west = isSketchTable(blockView, pos.west());
        boolean ne = isSketchTable(blockView, pos.north().east());
        boolean nw = isSketchTable(blockView, pos.north().west());
        boolean se = isSketchTable(blockView, pos.south().east());
        boolean sw = isSketchTable(blockView, pos.south().west());

        Consumer<Mesh> meshConsumer = context.meshConsumer();
        meshConsumer.accept(marked ? CORE_MARKED : CORE);
        meshConsumer.accept(NORTH[north ? 0 : 1]);
        meshConsumer.accept(EAST[east ? 0 : 1]);
        meshConsumer.accept(SOUTH[south ? 0 : 1]);
        meshConsumer.accept(WEST[west ? 0 : 1]);
        meshConsumer.accept(NE[north && east ? (ne ? 0 : 2) : (!north && !east) ? 2 : 1]);
        meshConsumer.accept(NW[north && west ? (nw ? 0 : 2) : (!north && !west) ? 2 : 1]);
        meshConsumer.accept(SE[south && east ? (se ? 0 : 2) : (!south && !east) ? 2 : 1]);
        meshConsumer.accept(SW[south && west ? (sw ? 0 : 2) : (!south && !west) ? 2 : 1]);
    }

    public boolean isClearSketchTable(BlockRenderView blockView, BlockPos pos) {
        return isSketchTable(blockView, pos) && !blockView.getBlockState(pos.up()).isOpaque();
    }

    public boolean isSketchTable(BlockRenderView blockView, BlockPos pos) {
        return blockView.getBlockState(pos).getBlock().equals(ModBlocks.SKETCHING_TABLE);
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
        context.meshConsumer().accept(ITEM);
    }
}
