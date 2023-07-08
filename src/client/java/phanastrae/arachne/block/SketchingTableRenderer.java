package phanastrae.arachne.block;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import org.joml.Math;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import phanastrae.arachne.Arachne;
import phanastrae.arachne.util.TableMultiblock;
import phanastrae.arachne.block.blockentity.SketchingTableBlockEntity;
import phanastrae.arachne.screen.editor.EditorMainScreen;
import phanastrae.arachne.weave.WeaveRenderer;

import java.util.Arrays;
import java.util.List;

public class SketchingTableRenderer implements BlockEntityRenderer<SketchingTableBlockEntity> {

    private final ItemRenderer itemRenderer;

    public SketchingTableRenderer(BlockEntityRendererFactory.Context ctx) {
        this.itemRenderer = ctx.getItemRenderer();
    }

    @Override
    public void render(SketchingTableBlockEntity entity, float tickDelta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light, int overlay) {
        try {
            BlockPos blockPos = entity.getPos();
            World world = entity.getWorld();

            TableMultiblock tableMultiblock = null;
            boolean inTable = false;
            if (MinecraftClient.getInstance().currentScreen instanceof EditorMainScreen editorMainScreen) {
                tableMultiblock = editorMainScreen.getScreenHandler().getTableMultiblock(entity.getWorld());
                if (tableMultiblock != null) {
                    TableMultiblock.BlockType blockType = tableMultiblock.getValue(entity.getPos());
                    inTable = TableMultiblock.isTypeInTable(blockType);
                }
            }

            this.renderItem(entity, tickDelta, matrixStack, vertexConsumerProvider, light, overlay);

            if (tableMultiblock == null) {
                HitResult hitResult = MinecraftClient.getInstance().crosshairTarget;
                if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK && world != null) {
                    if (((BlockHitResult) hitResult).getBlockPos().equals(blockPos)) {
                        TableMultiblock targetTMB = new TableMultiblock(blockPos);
                        targetTMB.init(world);

                        BlockPos nearestMarker = targetTMB.getNearestMarker(hitResult.getPos());
                        if (nearestMarker == null) {
                            nearestMarker = blockPos;
                            tableMultiblock = targetTMB;
                        } else {
                            tableMultiblock = getMarkerMultiblock(targetTMB, nearestMarker, world);
                        }
                        this.renderMarkers(targetTMB, nearestMarker, matrixStack, vertexConsumerProvider, light, overlay);
                        matrixStack.push();
                        Vec3i dif = nearestMarker.subtract(blockPos);
                        matrixStack.translate(dif.getX(), dif.getY(), dif.getZ());
                        this.renderMultiblockBorder(tableMultiblock, matrixStack, vertexConsumerProvider, light, overlay);
                        matrixStack.pop();
                    }
                }
            } else {
                if (tableMultiblock.isCenter(blockPos)) {
                    this.renderMultiblockBorder(tableMultiblock, matrixStack, vertexConsumerProvider, light, overlay);
                    if (!(MinecraftClient.getInstance().currentScreen instanceof EditorMainScreen editorMainScreen))
                        return;
                    WeaveRenderer.render(editorMainScreen, tickDelta, matrixStack, vertexConsumerProvider, light, overlay);
                }
            }
        } catch (ArrayStoreException e) {
            // TODO work out what the what
            // TODO remove? or fix?
            Arachne.LOGGER.info(Arrays.toString(e.getStackTrace()));
            Arachne.LOGGER.info(e.getMessage());
            Arachne.LOGGER.info(e.toString());
            CrashReport crashReport = new CrashReport("a", e);
            throw new CrashException(crashReport);
        }
    }

    public void renderItem(SketchingTableBlockEntity entity, float tickDelta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light, int overlay) {
        DefaultedList<ItemStack> defaultedList = entity.getItems();
        int lightLevel = entity.getWorld() != null && entity.getPos() != null ? WorldRenderer.getLightmapCoordinates(entity.getWorld(), entity.getPos().up()) : LightmapTextureManager.MAX_LIGHT_COORDINATE;
        int k = (int)entity.getPos().asLong();
        for (int l = 0; l < defaultedList.size(); ++l) {
            ItemStack itemStack = defaultedList.get(l);
            if (itemStack == ItemStack.EMPTY) continue;
            matrixStack.push();
            matrixStack.translate(0.5f, 1, 0.5f);
            if(l < entity.getRotations().length) {
                int r = entity.getRotations()[l];
                matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-r * 360f / SketchingTableBlockEntity.ROTATION_MAX));
            }
            matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0f));
            matrixStack.scale(0.75f, 0.75f, 0.75f);
            this.itemRenderer.renderItem(itemStack, ModelTransformationMode.FIXED, lightLevel, overlay, matrixStack, vertexConsumerProvider, entity.getWorld(), k + l);
            matrixStack.pop();
        }
    }

    public TableMultiblock getMarkerMultiblock(TableMultiblock tableMultiblock, BlockPos nearestMarker, World world) {
        if(nearestMarker != null) {
            tableMultiblock = new TableMultiblock(nearestMarker);
            tableMultiblock.init(world);
        }
        return tableMultiblock;
    }

    public void renderMultiblockBorder(TableMultiblock tableMultiblock, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light, int overlay) {
        List<BlockPos> tablePos = tableMultiblock.getBlocks(TableMultiblock.BlockType.TABLE);
        List<BlockPos> markedPos = tableMultiblock.getBlocks(TableMultiblock.BlockType.MARKED);

        matrixStack.push();
        matrixStack.translate(0.5f, 1, 0.5f);
        VertexConsumer vcLines = vertexConsumerProvider.getBuffer(RenderLayer.getLines());
        for(BlockPos pos : tablePos) {
            renderBorder(vcLines, matrixStack, tableMultiblock, pos, tableMultiblock.getCenter());
        }
        for(BlockPos pos : markedPos) {
            renderBorder(vcLines, matrixStack, tableMultiblock, pos, tableMultiblock.getCenter());
        }
        matrixStack.pop();
    }

    public void renderMarkers(TableMultiblock tableMultiblock, BlockPos nearestMarker, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light, int overlay) {
        List<BlockPos> reachableMarkerPos = tableMultiblock.getBlocks(TableMultiblock.BlockType.MARKED);

        VertexConsumer vcLines = vertexConsumerProvider.getBuffer(RenderLayer.getLines());
        for(BlockPos pos : reachableMarkerPos) {
            matrixStack.push();
            matrixStack.translate(0.5f, 1, 0.5f);
            Vec3i dif = pos.subtract(tableMultiblock.getCenter());
            matrixStack.translate(dif.getX(), dif.getY(), dif.getZ());
            if(pos.equals(nearestMarker)) {
                renderLine(vcLines, matrixStack, new Vec3d(0, 0, 0), new Vec3d(0, 0.5f, 0), 255, 255, 255, 255);
            } else {
                renderLine(vcLines, matrixStack, new Vec3d(0, 0, 0), new Vec3d(0, 0.25f, 0), 255, 255, 255, 127);
            }
            matrixStack.pop();
        }
    }

    public void renderBorder(VertexConsumer vcLines, MatrixStack matrixStack, TableMultiblock tableMultiblock, BlockPos pos, BlockPos targetPos) {
        matrixStack.push();
        matrixStack.translate(pos.getX() - targetPos.getX(), pos.getY() - targetPos.getY(), pos.getZ() - targetPos.getZ());
        for(Direction d : Direction.Type.HORIZONTAL) {
            Direction dcl = d.rotateYClockwise();
            Direction dccl = d.rotateYCounterclockwise();
            boolean dTable = TableMultiblock.isTypeInTable(tableMultiblock.getValue(pos.add(d.getVector())));
            boolean dclTable = TableMultiblock.isTypeInTable(tableMultiblock.getValue(pos.add(dcl.getVector())));
            boolean dcclTable = TableMultiblock.isTypeInTable(tableMultiblock.getValue(pos.add(dccl.getVector())));
            boolean dfclTable = TableMultiblock.isTypeInTable(tableMultiblock.getValue(pos.add(d.getVector()).add(dcl.getVector())));
            boolean dfcclTable = TableMultiblock.isTypeInTable(tableMultiblock.getValue(pos.add(d.getVector()).add(dccl.getVector())));
            Vec3d vd = new Vec3d(d.getUnitVector());
            Vec3d vdcl = new Vec3d(dcl.getUnitVector());
            float outer = 1/2f;
            float inner = 3/8f;
            if(!dTable) {
                float side1 = dclTable ? outer : inner;
                float side2 = dcclTable ? outer : inner;
                renderLine(vcLines, matrixStack, vd.multiply(inner).add(vdcl.multiply(side1)), vd.multiply(inner).subtract(vdcl.multiply(side2)), 255, 255, 255, 255);
            } else {
                if(dclTable & !dfclTable) {
                    renderLine(vcLines, matrixStack, vd.multiply(inner).add(vdcl.multiply(outer)), vd.multiply(inner).add(vdcl.multiply(inner)), 255, 255, 255, 255);
                }
                if(dcclTable & !dfcclTable) {
                    renderLine(vcLines, matrixStack, vd.multiply(inner).subtract(vdcl.multiply(outer)), vd.multiply(inner).subtract(vdcl.multiply(inner)), 255, 255, 255, 255);
                }
            }
        }

        matrixStack.pop();
    }

    public void renderLine(VertexConsumer vertexConsumer, MatrixStack matrices, Vec3d point1, Vec3d point2, int r, int g, int b, int a) {
        // TODO: check if should be drawing min to max?
        // TODO: combine with function from WeaveRenderer?
        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
        Matrix3f normalMatrix = matrices.peek().getNormalMatrix();
        float k = (float)Math.abs(point1.getX() - point2.getX());
        float l = (float)Math.abs(point1.getY() - point2.getY());
        float m = (float)Math.abs(point1.getZ() - point2.getZ());
        float n = MathHelper.sqrt((float)(k * k + l * l + m * m));
        vertexConsumer.vertex(positionMatrix, (float)point1.getX(), (float)point1.getY(), (float)point1.getZ())
                .color(r, g, b, a).normal(normalMatrix, k /= n, l /= n, m /= n).next();
        vertexConsumer.vertex(positionMatrix, (float)point2.getX(), (float)point2.getY(), (float)point2.getZ())
                .color(r, g, b, a).normal(normalMatrix, k, l, m).next();
    }
}
