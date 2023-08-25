package phanastrae.arachne.weave;

import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.GlAllocationUtils;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.*;
import org.joml.Math;
import org.lwjgl.system.MemoryUtil;
import phanastrae.arachne.Arachne;
import phanastrae.arachne.ArachneClient;
import phanastrae.arachne.CameraController;
import phanastrae.arachne.mixin.client.VertexBufferAccessor;
import phanastrae.arachne.render.*;
import phanastrae.arachne.old.PhysicsSystem;
import phanastrae.arachne.old.EditorMainScreen;
import phanastrae.arachne.util.ArachneMath;
import phanastrae.arachne.weave.element.built.BuiltFace;
import phanastrae.arachne.weave.element.built.BuiltRenderLayer;
import phanastrae.arachne.weave.element.built.BuiltSettings;
import phanastrae.arachne.weave.element.sketch.*;
import phanastrae.old.Face;
import phanastrae.old.Node;
import phanastrae.old.Weave;
import phanastrae.old.link_type.Link;
import phanastrae.old.link_type.StringLink;
import phanastrae.arachne.old.tools.FaceCreationTool;
import phanastrae.arachne.old.tools.SelectTool;
import phanastrae.arachne.util.TimerHolder;

import java.lang.management.MemoryUsage;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class WeaveRenderer {

    public static void updateEntityWeaves(Entity entity, float tickDelta, MatrixStack matrices, int light) {
        WeaveControl.forEachWeaveInEntity(entity, (string, weaveCache) -> updateEntityWeave(weaveCache.getWeave(), tickDelta, matrices, light));
    }

    public static void updateEntityWeave(@Nullable WeaveInstance weave, float tickDelta, MatrixStack matrices, int light) {
        if(weave == null) return;
        if(shouldCull(weave, matrices)) return;

        WeaveRenderer.queueInstanceUpdate(weave, tickDelta, light);
    }

    public static void renderEntityWeaves(Entity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        Profiler profiler = MinecraftClient.getInstance().getProfiler();
        profiler.push("arachne_entity_weaves");
        WeaveControl.forEachWeaveInEntity(entity, (string, weaveCache) -> renderEntityWeave(entity, weaveCache.getWeave(), yaw, tickDelta, matrices, vertexConsumers, light));
        profiler.pop();
    }

    public static void renderEntityWeave(Entity entity, @Nullable WeaveInstance weave, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        if(weave == null) return;

        if(shouldCull(weave, matrices)) return;

        // TODO reimplement nice weave movement

        matrices.push();
        matrices.translate(0, 0.5, 0);
        WeaveRenderer.renderInstance(weave, tickDelta, matrices, vertexConsumers, light, 0); // TODO: overlay?
        matrices.pop();
    }

    public static boolean shouldCull(WeaveInstance weave, MatrixStack matrices) {
        BuiltSettings settings = weave.builtWeave.settings;
        if(!settings.getDoCulling()) {
            return false;
        } else {
            matrices.push();
            matrices.translate(0, 0.5, 0);
            matrices.scale((float)settings.getWidth(), (float)settings.getHeight(), (float)settings.getWidth());
            Matrix4f projectionMatrix = RenderSystem.getProjectionMatrix();
            Matrix4f modelViewMatrix = RenderSystem.getModelViewMatrix();
            Matrix4f mat3 = matrices.peek().getPositionMatrix();
            Matrix4f mat = new Matrix4f();
            projectionMatrix.mul(modelViewMatrix, mat);
            mat.mul(mat3, mat);

            double minx = Double.POSITIVE_INFINITY;
            double maxx = Double.NEGATIVE_INFINITY;
            double miny = Double.POSITIVE_INFINITY;
            double maxy = Double.NEGATIVE_INFINITY;
            double minz = Double.POSITIVE_INFINITY;
            double maxz = Double.NEGATIVE_INFINITY;
            for(int i = 0; i < 8; i++) {
                float x = (i & 0x1) == 0 ? -0.5f : 0.5f;
                float y = (i & 0x2) == 0 ? -0.5f : 0.5f;
                float z = (i & 0x4) == 0 ? -0.5f : 0.5f;
                Vector4f v = mat.transform(new Vector4f(x, y, z, 1.0f));
                v.div(v.w);
                if(v.x < minx) minx = v.x;
                if(v.y < miny) miny = v.y;
                if(v.z < minz) minz = v.z;
                if(maxx < v.x) maxx = v.x;
                if(maxy < v.y) maxy = v.y;
                if(maxz < v.z) maxz = v.z;
            }
            matrices.pop();

            if(minx > 1) return true;
            if(miny > 1) return true;
            if(minz > 1) return true;
            if(maxx < -1) return true;
            if(maxy < -1) return true;
            if(maxz < -1) return true;
        }
        return false;
    }

    public static void renderEntityWeave(Entity entity, @Nullable Weave weave, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        if (weave == null) return;

        if (weave instanceof PhysicsSystem physicsSystem) {
            physicsSystem.windActive = true; // TODO

            Vec3d pos = entity.getLerpedPos(tickDelta).add(0, 0.5, 0);
            if (physicsSystem.lastPos != null) {
                physicsSystem.translateNonStaticNodes(physicsSystem.lastPos.subtract(pos));
            }
            physicsSystem.lastPos = pos;
        }

        // TODO: does this assume the entity always has a world? is that true?
        matrices.push();
        matrices.translate(0, 0.5, 0);
        WeaveRenderer.render(weave, tickDelta, matrices, vertexConsumers, light, 0); // TODO: overlay?
        matrices.pop();
    }

    public static void render(Weave weave, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        VertexConsumer vcSolid = vertexConsumers.getBuffer(ModRenderLayers.getSolid());
        Vec3d worldOffset = new Vec3d(0, 0, 0);
        if (weave instanceof PhysicsSystem physicsSystem && physicsSystem.lastPos != null) {
            worldOffset = physicsSystem.lastPos;
        }
        MinecraftClient.getInstance().getProfiler().push("arachne");
        TimerHolder.dualPush("weave_renderer");
        TimerHolder.dualPush("faces");
        for (Face face : weave.faces) {
            renderFace(tickDelta, vcSolid, matrices, face, worldOffset);
        }
        TimerHolder.dualPop();
        TimerHolder.dualPop();
        MinecraftClient.getInstance().getProfiler().pop();
    }

    public static void render(phanastrae.arachne.screen.editor.EditorMainScreen editorMainScreen, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        Vec3i blockPos = editorMainScreen.getScreenHandler().getPosition();
        Vec3d targetPos = CameraController.getInstance().targetPos;
        Vec3d targetOffset = targetPos.subtract(blockPos.getX(), blockPos.getY(), blockPos.getZ());

        matrices.push();
        matrices.translate(0.5, 1.5, 0.5);
        if(editorMainScreen.weaveInstance == null) {
            renderSketch(editorMainScreen.editorInstance.getSketchWeave(), tickDelta, matrices, vertexConsumers, light, overlay);
        } else {
            renderInstance(editorMainScreen.weaveInstance, tickDelta, matrices, vertexConsumers, light, overlay);
        }
        editorMainScreen.editorInstance.render(tickDelta, matrices, vertexConsumers);
        matrices.pop();


        matrices.push();
        matrices.translate(targetOffset.x, targetOffset.y, targetOffset.z);
        renderCrosshair(matrices, vertexConsumers.getBuffer(RenderLayer.LINES));
        matrices.pop();
    }

    public static void renderSketch(SketchWeave sketch, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        MinecraftClient.getInstance().getProfiler().push("arachne_editor_sketch");
        VertexConsumerProvider.Immediate vcpi = ModRenderLayers.getBuffers();
        setupNodeWorldPos(sketch);
        renderNodes(sketch.nodes, matrices, vcpi);
        renderEdges(sketch.edges, matrices, vcpi);
        renderFaces(sketch.faces, matrices, vcpi);
        //renderTransforms(sketch.getRigidBodies(), matrices, vcpi);
        vcpi.draw();
        MinecraftClient.getInstance().getProfiler().pop();
    }

    public static void queueInstanceUpdate(WeaveInstance instance, float tickDelta, int light) {
        Profiler profiler = MinecraftClient.getInstance().getProfiler();

        profiler.push("setup");
        setupWeaveInstance(instance, tickDelta, light);
        profiler.pop();

        profiler.push("queueUpdate");
        instance.setBufferUpdating();
        instance.bufferReady = true;
        ArachneClient.runnableQueueClient.queue(() -> {
            instance.waitForUpdate();
            updateWeaveInstance(instance, tickDelta, light);
            instance.setBufferUpdating(false);
        });
        profiler.pop();
    }

    public static void renderInstance(WeaveInstance instance, float tickDelta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        Profiler profiler = MinecraftClient.getInstance().getProfiler();
        profiler.push("arachne_weave");

        profiler.push("waitForPhysicsUpdate");
        instance.waitForUpdate();

        instance.lock();
        try {
            profiler.pop();

            if(instance.bufferReady) {
                profiler.push("waitForRenderUpdate");
                instance.waitForBufferUpdate();
                instance.bufferReady = false;
                profiler.pop();
            } else {
                profiler.push("setup");
                setupWeaveInstance(instance, tickDelta, light);
                profiler.pop();

                profiler.push("update");
                instance.setBufferUpdating();
                ArachneClient.runnableQueueClient.queue(() -> {
                    updateWeaveInstance(instance, tickDelta, light);
                    instance.setBufferUpdating(false);
                });
                profiler.pop();
                profiler.push("waitForRenderUpdate");
                instance.waitForBufferUpdate();
                profiler.pop();
            }

            profiler.push("render");
            renderWeaveInstance(instance, matrixStack);
            profiler.pop();
        } finally {
            instance.unlock();
        }

        profiler.pop();
    }

    public static void setupWeaveInstance(WeaveInstance instance, float tickDelta, int light) {
        BuiltRenderLayer[] renderLayers = instance.builtWeave.renderLayers;
        for (BuiltRenderLayer rl : renderLayers) {
            setupLayer(rl, instance);
        }
    }

    public static void updateWeaveInstance(WeaveInstance instance, float tickDelta, int light) {
        instance.doLerp(tickDelta);
        BuiltRenderLayer[] renderLayers = instance.builtWeave.renderLayers;
        for (BuiltRenderLayer rl : renderLayers) {
            updateLayer(rl, instance, light);
        }
    }

    public static void renderWeaveInstance(WeaveInstance instance, MatrixStack matrixStack) {
        MatrixStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.push();
        modelViewStack.multiplyPositionMatrix(matrixStack.peek().getPositionMatrix());
        RenderSystem.applyModelViewMatrix();

        BuiltRenderLayer[] renderLayers = instance.builtWeave.renderLayers;
        for(BuiltRenderLayer rl : renderLayers) {
            renderLayer(rl, instance);
        }
        modelViewStack.pop();
        RenderSystem.applyModelViewMatrix();
    }

    public static void setupLayer(BuiltRenderLayer layer, WeaveInstance instance) {
        // setup or get vertex buffer
        VertexBufferHolder vbh;
        VertexBuffer vertexBuffer;
        if(!(layer.vertexBufferHolder instanceof VertexBufferHolder verb) || verb.getBuffer().isClosed()) {
            vertexBuffer = new VertexBuffer(VertexBuffer.Usage.DYNAMIC);
            vbh = new VertexBufferHolder(vertexBuffer);
            BufferHolders.storeBufferHolder(vbh);
            layer.vertexBufferHolder = vbh;

            // gen default buffers
            int vertexCount = layer.getVertexCount();
            int indexCount = layer.getIndexCount();

            // setup index buffer
            ByteBuffer vb = GlAllocationUtils.allocateByteBuffer(vertexCount * 32);
            ByteBuffer indexBuffer = GlAllocationUtils.allocateByteBuffer(indexCount * 4);
            int index = 0;
            int vertex = 0;
            for(BuiltFace face : layer.faces) {
                int k = face.nodes.length;
                for(int i = 0; i < k; i++) {
                    int j = (i + 1)%k;
                    indexBuffer.putInt(index * 4, vertex + i);
                    indexBuffer.putInt((index+1) * 4, vertex + j);
                    indexBuffer.putInt((index+2) * 4, vertex + k);
                    index += 3;
                }
                vertex += (k+1);
                if(face.doubleSided) {
                    for(int i = 0; i < k; i++) {
                        int j = (i + 1)%k;
                        indexBuffer.putInt(index * 4, vertex + j);
                        indexBuffer.putInt((index+1) * 4, vertex + i);
                        indexBuffer.putInt((index+2) * 4, vertex + k);
                        index += 3;
                    }
                    vertex += (k+1);
                }
            }

            // upload default buffers
            vertexBuffer.bind();
            BufferBuilder.DrawParameters params = new BufferBuilder.DrawParameters(VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL, vertexCount, indexCount, VertexFormat.DrawMode.TRIANGLES, VertexFormat.IndexType.INT, false, false);
            ((VertexBufferAccess)vertexBuffer).upload(params, vb, indexBuffer);
            VertexBuffer.unbind();

            // free buffers
            MemoryUtil.memFree(vb);
            MemoryUtil.memFree(indexBuffer);
        } else {
            vbh = (VertexBufferHolder) layer.vertexBufferHolder;
            vertexBuffer = vbh.getBuffer();
        }

        // input dynamic data
        ByteBuffer vb = null;
        ByteBufferHolder bbh = null;
        boolean needsGen = true;
        if(instance.layerToBufferHolderMap.containsKey(layer)) {
            Object o = instance.layerToBufferHolderMap.get(layer);
            if(o instanceof ByteBufferHolder) {
                bbh = (ByteBufferHolder)o;
                if(bbh.isReleased()) {
                    instance.layerToBufferHolderMap.remove(layer);
                } else {
                    needsGen = false;
                    vb = bbh.getBuffer();
                }
            } else {
                instance.layerToBufferHolderMap.remove(layer);
            }
        }
        if(needsGen) {
            vb = GlAllocationUtils.allocateByteBuffer(layer.getVertexCount() * 32);
            setupLayer(layer);
            initBuffer(vb, layer, instance);
            bbh = new ByteBufferHolder(vb);
            bbh.setLastReload(ArachneClient.latestReload);
            BufferHolders.storeBufferHolder(bbh);
            instance.layerToBufferHolderMap.put(layer, bbh);
        }
    }

    public static void updateLayer(BuiltRenderLayer layer, WeaveInstance instance, int light) {
        ByteBufferHolder bbh;
        ByteBuffer vb;
        if(instance.layerToBufferHolderMap.get(layer) instanceof ByteBufferHolder bybh) {
            bbh = bybh;
            vb = bbh.getBuffer();
        } else {
            return;
        }

        long latestReload = ArachneClient.latestReload;
        if(bbh.needsReload(latestReload)) {
            // setup uvs in case of resource pack reload
            setupLayer(layer);
            initBuffer(vb, layer, instance);
            bbh.setLastReload(latestReload);
        }

        updateBuffer(vb, layer, instance, light);
    }

    public static void renderLayer(BuiltRenderLayer layer, WeaveInstance instance) {
        VertexBuffer vertexBuffer;
        ByteBuffer vb;
        if(layer.vertexBufferHolder instanceof VertexBufferHolder vbh) {
            vertexBuffer = vbh.getBuffer();
            if(vertexBuffer.isClosed()) {
                return;
            }
        } else {
            return;
        }
        if(instance.layerToBufferHolderMap.get(layer) instanceof ByteBufferHolder bbh) {
            vb = bbh.getBuffer();
            if(bbh.isReleased()) {
                return;
            }
        } else {
            return;
        }


        GlStateManager._glBindBuffer(GlConst.GL_ARRAY_BUFFER, ((VertexBufferAccessor)vertexBuffer).getVertexBufferId());
        RenderSystem.glBufferData(GlConst.GL_ARRAY_BUFFER, vb, GlConst.GL_DYNAMIC_DRAW);

        // render
        RenderLayer renderLayer = getRenderLayer(layer);
        vertexBuffer.bind();

        renderLayer.startDrawing();
        vertexBuffer.draw(RenderSystem.getModelViewMatrix(), RenderSystem.getProjectionMatrix(), RenderSystem.getShader());
        renderLayer.endDrawing();

        VertexBuffer.unbind();
    }

    public static void setupLayer(BuiltRenderLayer layer) {
        Identifier id = layer.getIdentifier();
        boolean useAtlas = layer.getUseTextureAtlas();
        long latestReload = ArachneClient.latestReload;
        if(layer.needsUpdate(latestReload)) {
            if (id != null && useAtlas) {
                Function<Identifier, Sprite> ATLAS = MinecraftClient.getInstance().getSpriteAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE);
                Sprite sprite = ATLAS.apply(id);
                if (sprite != null) {
                    layer.setUVs(sprite.getMinU(), sprite.getMinV(), sprite.getMaxU(), sprite.getMaxV(), latestReload);
                }
            }
        }
    }

    public static RenderLayer getRenderLayer(BuiltRenderLayer layer) {
        Identifier id = layer.getIdentifier();
        boolean useAtlas = layer.getUseTextureAtlas();
        return useAtlas ? ModRenderLayers.getSolid() : ModRenderLayers.SOLID_TEXTURE.apply(id, true);
    }

    public static void updateBuffer(ByteBuffer vertexBuffer, BuiltRenderLayer layer, WeaveInstance instance, int light) {
        WeaveStateUpdater stateUpdater = instance.lerpWeaveStateUpdater;

        ArrayList<BuiltFace> faces = layer.faces;
        ArrayList<Byte> rs = layer.rs;
        ArrayList<Byte> gs = layer.gs;
        ArrayList<Byte> bs = layer.bs;
        ArrayList<Byte> as = layer.as;

        Direction[] directions = new Direction[]{Direction.EAST, Direction.WEST, Direction.UP, Direction.DOWN, Direction.SOUTH, Direction.NORTH};
        float[] directionColors = new float[]{1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f};
        World world = MinecraftClient.getInstance().world;
        if(world != null) {
            for (int i = 0; i < 6; i++) {
                directionColors[i] = world.getBrightness(directions[i], true);
            }
        }

        int vertexOffset = 0;
        float[] p = new float[3];
        float[] p3 = new float[3];
        float[] normal = new float[3];
        byte[] common1 = new byte[4];
        byte[] common2 = new byte[7];
        for(int i = 0; i < faces.size(); i++) {
            BuiltFace face = faces.get(i);
            byte r = rs.get(i);
            byte g = gs.get(i);
            byte b = bs.get(i);
            byte a = as.get(i);

            face.getCenterPos(stateUpdater, p3);
            face.getNormal(stateUpdater, normal, p3);
            float nx = normal[0];
            float ny = normal[1];
            float nz = normal[2];
            int inx = (int)(nx*127.0f);
            int iny = (int)(ny*127.0f);
            int inz = (int)(nz*127.0f);
            int l = face.nodes.length;

            short l1 = (short)(light & (LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE | 0xFF0F));
            short l2 = (short)(light >> 16 & (LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE | 0xFF0F));

            float lmul = nx*nx*(nx > 0 ? directionColors[0] : directionColors[1])
                    + ny*ny*(ny > 0 ? directionColors[2] : directionColors[3])
                    + nz*nz*(nz > 0 ? directionColors[4] : directionColors[5]);
            if(nx*nx+ny*ny+nz*nz == 0) { // if overall face normal is 0, set light to 1
                lmul = 1.0f;
            }
            byte rl = (byte)((r&0xff) * lmul);
            byte gl = (byte)((g&0xff) * lmul);
            byte bl = (byte)((b&0xff) * lmul);

            byte bnx = (byte)(inx&0xff);
            byte bny = (byte)(iny&0xff);
            byte bnz = (byte)(inz&0xff);

            common1[0] = rl;
            common1[1] = gl;
            common1[2] = bl;
            common1[3] = a;

            common2[0] = (byte)(l1&0xff);
            common2[1] = (byte)((l1&0xff00)>>8);
            common2[2] = (byte)(l2&0xff);
            common2[3] = (byte)((l2&0xff00)>>8);
            common2[4] = bnx;
            common2[5] = bny;
            common2[6] = bnz;

            for(int j = 0; j < l; j++) {
                face.getNodeInput(j, stateUpdater).getPosition(p);
                updateVertex(vertexBuffer, vertexOffset, p[0], p[1], p[2], common1, common2);
                vertexOffset++;
            }
            updateVertex(vertexBuffer, vertexOffset, p3[0], p3[1], p3[2], common1, common2);
            vertexOffset++;

            if(face.doubleSided) {
                lmul = nx*nx*(nx < 0 ? directionColors[0] : directionColors[1])
                        + ny*ny*(ny < 0 ? directionColors[2] : directionColors[3])
                        + nz*nz*(nz < 0 ? directionColors[4] : directionColors[5]);
                if(nx*nx+ny*ny+nz*nz == 0) { // if overall face normal is 0, set light to 1
                    lmul = 1.0f;
                }
                rl = (byte)((r&0xff) * lmul);
                gl = (byte)((g&0xff) * lmul);
                bl = (byte)((b&0xff) * lmul);

                bnx = (byte)(-(inx)&0xff);
                bny = (byte)(-(iny)&0xff);
                bnz = (byte)(-(inz)&0xff);


                common1[0] = rl;
                common1[1] = gl;
                common1[2] = bl;

                common2[4] = bnx;
                common2[5] = bny;
                common2[6] = bnz;

                for (int j = 0; j < l; j++) {
                    face.getNodeInput(j, stateUpdater).getPosition(p);
                    updateVertex(vertexBuffer, vertexOffset, p[0], p[1], p[2], common1, common2);
                    vertexOffset++;
                }
                updateVertex(vertexBuffer, vertexOffset, p3[0], p3[1], p3[2], common1, common2);
                vertexOffset++;
            }
        }
    }

    public static void updateVertex(ByteBuffer vertexBuffer, int vertexOffset, float x, float y, float z, byte[] common1, byte[] common2) {
        int offset = vertexOffset * 32;
        vertexBuffer.putFloat(offset, x);
        vertexBuffer.putFloat(offset+4, y);
        vertexBuffer.putFloat(offset+8, z);
        vertexBuffer.put(offset+12, common1);
        //vertexBuffer.put(offset+12, r);
        //vertexBuffer.put(offset+13, g);
        //vertexBuffer.put(offset+14, b);
        //vertexBuffer.put(offset+15, a);
        // skip uvs, update not needed
        vertexBuffer.put(offset+24, common2);
        //vertexBuffer.putShort(offset+24, l1);
        //vertexBuffer.putShort(offset+26, l2);
        //vertexBuffer.put(offset+28, nx);
        //vertexBuffer.put(offset+29, ny);
        //vertexBuffer.put(offset+30, nz);
        // final byte is always empty
    }

    public static void initBuffer(ByteBuffer vertexBuffer, BuiltRenderLayer layer, WeaveInstance instance) {
        ArrayList<BuiltFace> faces = layer.faces;
        ArrayList<float[]> us = layer.usClean;
        ArrayList<float[]> vs = layer.vsClean;
        ArrayList<Float> uAvgs = layer.uAvgClean;
        ArrayList<Float> vAvgs = layer.vAvgClean;

        int vertexOffset = 0;
        for(int i = 0; i < faces.size(); i++) {
            BuiltFace face = faces.get(i);
            float[] u = us.get(i);
            float[] v = vs.get(i);
            float uAvg = uAvgs.get(i);
            float vAvg = vAvgs.get(i);
            int l = face.nodes.length;

            for(int j = 0; j < l; j++) {
                initVertex(vertexBuffer, vertexOffset, u[j], v[j]);
                vertexOffset++;
            }
            initVertex(vertexBuffer, vertexOffset, uAvg, vAvg);
            vertexOffset++;

            if(face.doubleSided) {
                for(int j = 0; j < l; j++) {
                    initVertex(vertexBuffer, vertexOffset, u[j], v[j]);
                    vertexOffset++;
                }
                initVertex(vertexBuffer, vertexOffset, uAvg, vAvg);
                vertexOffset++;
            }
        }
    }

    public static void initVertex(ByteBuffer vertexBuffer, int vertexOffset, float u, float v) {
        int offset = vertexOffset * 32;
        vertexBuffer.put(offset, new byte[32]);
        vertexBuffer.putFloat(offset+16, u);
        vertexBuffer.putFloat(offset+20, v);
    }

    public static void renderLayer(BuiltRenderLayer layer, WeaveInstance instance, VertexConsumer vc, int light) {
        WeaveStateUpdater stateUpdater = instance.lerpWeaveStateUpdater;

        ArrayList<BuiltFace> faces = layer.faces;
        ArrayList<Byte> rs = layer.rs;
        ArrayList<Byte> gs = layer.gs;
        ArrayList<Byte> bs = layer.bs;
        ArrayList<Byte> as = layer.as;
        ArrayList<float[]> us = layer.usClean;
        ArrayList<float[]> vs = layer.vsClean;
        ArrayList<Float> uAvgs = layer.uAvgClean;
        ArrayList<Float> vAvgs = layer.vAvgClean;

        Direction[] directions = new Direction[]{Direction.EAST, Direction.WEST, Direction.UP, Direction.DOWN, Direction.SOUTH, Direction.NORTH};
        float[] directionColors = new float[]{1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f};
        World world = MinecraftClient.getInstance().world;
        if(world != null) {
            for (int i = 0; i < 6; i++) {
                directionColors[i] = world.getBrightness(directions[i], true);
            }
        }

        SolidBufferBuilder sbb = null;
        boolean SBB = vc instanceof SolidBufferBuilder;
        if(SBB) sbb = (SolidBufferBuilder)vc;

        for(int i = 0; i < faces.size(); i++) {
            BuiltFace face = faces.get(i);
            byte r = rs.get(i);
            byte g = gs.get(i);
            byte b = bs.get(i);
            byte a = as.get(i);
            float[] u = us.get(i);
            float[] v = vs.get(i);
            float uAvg = uAvgs.get(i);
            float vAvg = vAvgs.get(i);

            Vec3d p3 = face.getCenterPos(stateUpdater);
            Vec3d faceNormal = face.getNormal(stateUpdater);
            int l = face.nodes.length;
            for (int j = 0; j < l; j++) {
                int k = (j+1)%l;
                float[] uvs = {u[j], v[j], u[k], v[k], uAvg, vAvg};
                Vec3d p1 = face.getNodeInput(j, stateUpdater).getPosition();
                Vec3d p2 = face.getNodeInput(k, stateUpdater).getPosition();
                if(SBB) {
                    drawTriangle(sbb, p1, p2, p3, r, g, b, a, uvs, (float)faceNormal.x, (float)faceNormal.y, (float)faceNormal.z, directionColors, light);
                    if (face.doubleSided) {
                        uvs = new float[]{u[k], v[k], u[j], v[j], uAvg, vAvg};
                        drawTriangle(sbb, p2, p1, p3, r, g, b, a, uvs, -(float)faceNormal.x, -(float)faceNormal.y, -(float)faceNormal.z, directionColors, light);
                    }
                } else {
                    drawTriangle(vc, p1, p2, p3, r, g, b, a, uvs, (float)faceNormal.x, (float)faceNormal.y, (float)faceNormal.z, directionColors, light);
                    if (face.doubleSided) {
                        uvs = new float[]{u[k], v[k], u[j], v[j], uAvg, vAvg};
                        drawTriangle(vc, p2, p1, p3, r, g, b, a, uvs, -(float)faceNormal.x, -(float)faceNormal.y, -(float)faceNormal.z, directionColors, light);
                    }
                }
            }
        }
    }

    public static void drawTriangle(VertexConsumer vc, Vec3d p1, Vec3d p2, Vec3d p3, byte r, byte g, byte b, byte a, float[] uvs, float nx, float ny, float nz, float[] directionColors, int light) {
        float l = nx*nx*(nx > 0 ? directionColors[0] : directionColors[1])
                + ny*ny*(ny > 0 ? directionColors[2] : directionColors[3])
                + nz*nz*(nz > 0 ? directionColors[4] : directionColors[5]);
        if(nx*nx+ny*ny+nz*nz == 0) { // if overall face normal is 0, set light to 1
            l = 1.0f;
        }

        float rf = ((r&0xff) * l)/255f;
        float gf = ((g&0xff) * l)/255f;
        float bf = ((b&0xff) * l)/255f;
        float af = a/255f;

        //Vector4f v1 = mat.transform(new Vector4f((float)p1.x, (float)p1.y, (float)p1.z, 1));
        //Vector4f v2 = mat.transform(new Vector4f((float)p2.x, (float)p2.y, (float)p2.z, 1));
        //Vector4f v3 = mat.transform(new Vector4f((float)p3.x, (float)p3.y, (float)p3.z, 1));

        vc.vertex((float)p1.x, (float)p1.y, (float)p1.z, rf, gf, bf, af, uvs[0], uvs[1], 0, light, nx, ny, nz);
        vc.vertex((float)p2.x, (float)p2.y, (float)p2.z, rf, gf, bf, af, uvs[2], uvs[3], 0, light, nx, ny, nz);
        vc.vertex((float)p3.x, (float)p3.y, (float)p3.z, rf, gf, bf, af, uvs[4], uvs[5], 0, light, nx, ny, nz);
    }

    public static void drawTriangle(SolidBufferBuilder vc, Vec3d p1, Vec3d p2, Vec3d p3, byte r, byte g, byte b, byte a, float[] uvs, float nx, float ny, float nz, float[] directionColors, int light) {
        float l = nx*nx*(nx > 0 ? directionColors[0] : directionColors[1])
            + ny*ny*(ny > 0 ? directionColors[2] : directionColors[3])
            + nz*nz*(nz > 0 ? directionColors[4] : directionColors[5]);
        if(nx*nx+ny*ny+nz*nz == 0) { // if overall face normal is 0, set light to 1
            l = 1.0f;
        }

        byte rb = (byte)((r&0xff)*l);
        byte gb = (byte)((g&0xff)*l);
        byte bb = (byte)((b&0xff)*l);
        byte ab = (byte)(a);

        //Vector4f v1 = mat.transform(new Vector4f((float)p1.x, (float)p1.y, (float)p1.z, 1));
        //Vector4f v2 = mat.transform(new Vector4f((float)p2.x, (float)p2.y, (float)p2.z, 1));
        //Vector4f v3 = mat.transform(new Vector4f((float)p3.x, (float)p3.y, (float)p3.z, 1));

        vc.accept(p1, p2, p3, rb, gb, bb, ab, uvs, light, nx, ny, nz);
    }

    public static void renderCrosshair(MatrixStack matrices, VertexConsumer vcLines) {
        float l = 1/32f;
        matrices.push();
        drawCrosshair(vcLines, matrices, 0, l);
        matrices.pop();
    }

    static void setupNodeWorldPos(SketchWeave sketchWeave) {
        MatrixStack matrices = new MatrixStack();
        for(SketchTransform transform : sketchWeave.transforms) {
            if(transform.parent == null) {
                setupNodeWorldPosTree(transform, matrices);
            }
        }
    }

    static void setupNodeWorldPosTree(SketchTransform transform, MatrixStack matrices) {
        if(transform.children == null) return;
        matrices.push();
        matrices.translate(transform.x, transform.y, transform.z);
        matrices.multiply(new Quaternionf().rotateYXZ((float)transform.getYawRad(), (float)transform.getPitchRad(), (float)transform.getRollRad()));
        matrices.scale((float)transform.sizex, (float)transform.sizey, (float)transform.sizez);
        for(SketchElement el : transform.children) {
            if(el instanceof SketchTransform e) {
                setupNodeWorldPosTree(e, matrices);
            } else if(el instanceof SketchVertex e) {
                Vector4f v = new Vector4f((float)e.x, (float)e.y, (float)e.z, 1);
                v = v.mul(matrices.peek().getPositionMatrix());
                e.gx = v.x;
                e.gy = v.y;
                e.gz = v.z;
            }
        }
        matrices.pop();
    }

    public static void renderNodes(List<SketchVertex> nodes, MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
        VertexConsumer circles = vertexConsumers.getBuffer(ModRenderLayers.getDisk());
        Matrix4f posMatrix = matrices.peek().getPositionMatrix();
        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
        Vec3d camLook = CameraController.getCameraLookVector(camera);

        double yaw = Math.toRadians(camera.getYaw());
        double pitch = Math.toRadians(camera.getPitch());
        float sy = (float)Math.sin(yaw);
        float cy = (float)Math.cos(yaw);
        float sp = (float)Math.sin(pitch);
        float cp = (float)Math.cos(pitch);
        for(SketchVertex node : nodes) {
            Vec3d pos = new Vec3d(node.gx, node.gy, node.gz);
            float x = (float)pos.x;
            float y = (float)pos.y;
            float z = (float)pos.z;
            Vec3d offset = pos.add(CameraController.getInstance().originPos.subtract(camera.getPos()));
            float d = (float)offset.dotProduct(camLook);
            if(d > 1) {
                d = 1;
            }
            d *= 1/4f;
            float o1x = -sy*sp / 16 * d;
            float o1y = cp / 16 * d;
            float o1z = cy*sp / 16 * d;
            float o2x = -cy / 16 * d;
            float o2y = 0 * d;
            float o2z = -sy / 16 * d;
            int r;
            int g;
            int b;
            if(node.highlighted) {
                r = 255;
                g = 255;
                b = 255;
            } else if(node.selected) {
                r = 255;
                g = 127;
                b = 63;
            } else {
                r = 127;
                g = 191;
                b = 191;
            }
            circles.vertex(posMatrix, x+o1x+o2x, y+o1y+o2y, z+o1z+o2z).color(r, g, b, 255).texture(0, 0).next();
            circles.vertex(posMatrix, x+o1x-o2x, y+o1y-o2y, z+o1z-o2z).color(r, g, b, 255).texture(1, 0).next();
            circles.vertex(posMatrix, x-o1x-o2x, y-o1y-o2y, z-o1z-o2z).color(r, g, b, 255).texture(1, 1).next();
            circles.vertex(posMatrix, x-o1x+o2x, y-o1y+o2y, z-o1z+o2z).color(r, g, b, 255).texture(0, 1).next();
        }
    }

    public static void renderEdges(List<SketchEdge> edges, MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
        VertexConsumer lines = vertexConsumers.getBuffer(RenderLayer.LINES);
        for(SketchEdge edge : edges) {
            int r;
            int g;
            int b;
            if(edge.highlighted) {
                r = 255;
                g = 255;
                b = 255;
            } else if(edge.selected) {
                r = 255;
                g = 127;
                b = 63;
            } else {
                r = 127;
                g = 191;
                b = 191;
            }
            drawLine(lines, matrices, edge.start.gx, edge.start.gy, edge.start.gz, edge.end.gx, edge.end.gy, edge.end.gz, r, g, b, 255);
        }
    }

    public static void renderFaces(List<SketchFace> faces, MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
        VertexConsumer vc = vertexConsumers.getBuffer(ModRenderLayers.getPosColorTriangles());
        if(vc instanceof PosColorBufferBuilder pcbb) {
            for (SketchFace face : faces) {
                renderFace(pcbb, matrices, face);
            }
        }
    }

    public static void renderFace(PosColorBufferBuilder vc, MatrixStack matrices, SketchFace face) {
        if(face.vertices.length < 3) return;
        Vec3d camLook = CameraController.getCameraLookVector(MinecraftClient.getInstance().gameRenderer.getCamera());
        int r;
        int g;
        int b;
        if(face.highlighted) {
            r = 0xDF;
            g = 0xDF;
            b = 0xDF;
        } else if(face.selected) {
            r = 0xDF;
            g = 0x6F;
            b = 0x2F;
        } else {
            r = 0x6F;
            g = 0xAF;
            b = 0xAF;
        }
        Vec3d avgPos = face.getAvgGlobalPos();
        if(avgPos == null) return;
        for(int i = 0; i < face.vertices.length; i++) {
            Vec3d p1 = face.vertices[i].getLastGlobalPos();
            Vec3d p2 = face.vertices[(i+1)%face.vertices.length].getLastGlobalPos();
            drawTriangle(vc, matrices, p1, p2, avgPos, r, g, b, 0xFF, camLook);
            if(face.isDoubleSided()) {
                drawTriangle(vc, matrices, p2, p1, avgPos, r, g, b, 0xFF, camLook);
            }
        }
    }


    public static void drawTriangle(PosColorBufferBuilder pcbb, MatrixStack matrices, Vec3d p1, Vec3d p2, Vec3d p3, int r, int g, int b, int a, Vec3d camLook) {
        Vec3d normal = ArachneMath.getNormal(p1, p2, p3);

        // whilst in editor view, do lighting such that the camera provides the light
        double lightMultiplier = 0.6 + 0.4 * (-normal.dotProduct(camLook));

        byte rf = (byte) (r * lightMultiplier);
        byte gf = (byte) (g * lightMultiplier);
        byte bf = (byte) (b * lightMultiplier);

        Matrix4f mat = matrices.peek().getPositionMatrix();
        pcbb.accept(mat, p1, p2, p3, rf, gf, bf, (byte)(a&0xFF));
    }

    public static void drawLine(VertexConsumer vertexConsumer, MatrixStack matrices, double x1, double y1, double z1, double x2, double y2, double z2, int r, int g, int b, int a) {
        // TODO: check if should be drawing min to max?
        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
        Matrix3f normalMatrix = matrices.peek().getNormalMatrix();
        float k = (float)Math.abs(x1-x2);
        float l = (float)Math.abs(y1-y2);
        float m = (float)Math.abs(z1-z2);
        float n = MathHelper.sqrt((k * k + l * l + m * m));
        vertexConsumer.vertex(positionMatrix, (float)x1, (float)y1, (float)z1)
                .color(r, g, b, a).normal(normalMatrix, k /= n, l /= n, m /= n).next();
        vertexConsumer.vertex(positionMatrix, (float)x2, (float)y2, (float)z2)
                .color(r, g, b, a).normal(normalMatrix, k, l, m).next();
    }


    public static void renderTransforms(List<SketchTransform> rigidBodies, MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
        // draws frames and axes for rigid bodies

        VertexConsumer vcLines = vertexConsumers.getBuffer(RenderLayer.LINES);
        for(SketchTransform rb : rigidBodies) {
            if(rb.parent == null) {
                drawTransformTree(vcLines, matrices, rb);
            }
        }
    }

    static void drawTransformTree(VertexConsumer vcLines, MatrixStack matrices, SketchTransform transform) {
        matrices.push();
        matrices.translate(transform.x, transform.y, transform.z);
        // For some reason Vanilla's entity models use ZYX rotation and also some weird axis sign flipping, but just doing the vertical axis Y (yaw) first and
        // also no axis sign flipping seems to make far more sense and is consistent with other similar editors,
        // so Arachne RigidBodies will use that.
        // The ordering of X and Z probably shouldn't matter as much, so YXZ will be used because
        // 1) vanilla labels X pitch and Z roll and yaw->pitch->roll makes the most sense, and
        // b) there is no Quaternionf().rotateYZX function
        // negate angles so that rotation is clockwise about axes to be consistent with some other editors
        matrices.multiply(new Quaternionf().rotateYXZ((float)transform.getYawRad(), (float)transform.getPitchRad(), (float)transform.getRollRad()));
        matrices.scale((float)transform.sizex, (float)transform.sizey, (float)transform.sizez);
        drawRigidBody(vcLines, matrices, transform);
        if(transform.children != null) {
            for (SketchElement child : transform.children) {
                if(child instanceof SketchTransform t) {
                    drawTransformTree(vcLines, matrices, t);
                }
            }
        }
        matrices.pop();
    }

    static void drawRigidBody(VertexConsumer vcLines, MatrixStack matrices, SketchTransform transform) {
        drawCrosshair(vcLines, matrices, 0.375, 0.625);

        int r;
        int g;
        int b;
        if(transform.highlighted) {
            r = 255;
            g = 255;
            b = 255;
        } else if(transform.selected) {
            r = 255;
            g = 127;
            b = 63;
        } else if(!transform.canDelete()) {
            r = 0x9F;
            g = 0xAF;
            b = 0xAF;
        } else {
            r = 127;
            g = 191;
            b = 191;
        }

        Vec3d vppp = new Vec3d(0.5, 0.5, 0.5);
        Vec3d vppn = new Vec3d(0.5, 0.5, -0.5);
        Vec3d vpnp = new Vec3d(0.5, -0.5, 0.5);
        Vec3d vpnn = new Vec3d(0.5, -0.5, -0.5);
        Vec3d vnpp = new Vec3d(-0.5, 0.5, 0.5);
        Vec3d vnpn = new Vec3d(-0.5, 0.5, -0.5);
        Vec3d vnnp = new Vec3d(-0.5, -0.5, 0.5);
        Vec3d vnnn = new Vec3d(-0.5, -0.5, -0.5);
        drawLine(vcLines, matrices, vppp, vppn, r, g, b, 0xFF);
        drawLine(vcLines, matrices, vpnp, vpnn, r, g, b, 0xFF);
        drawLine(vcLines, matrices, vnpp, vnpn, r, g, b, 0xFF);
        drawLine(vcLines, matrices, vnnp, vnnn, r, g, b, 0xFF);
        drawLine(vcLines, matrices, vppp, vpnp, r, g, b, 0xFF);
        drawLine(vcLines, matrices, vppn, vpnn, r, g, b, 0xFF);
        drawLine(vcLines, matrices, vnpp, vnnp, r, g, b, 0xFF);
        drawLine(vcLines, matrices, vnpn, vnnn, r, g, b, 0xFF);
        drawLine(vcLines, matrices, vppp, vnpp, r, g, b, 0xFF);
        drawLine(vcLines, matrices, vppn, vnpn, r, g, b, 0xFF);
        drawLine(vcLines, matrices, vpnp, vnnp, r, g, b, 0xFF);
        drawLine(vcLines, matrices, vpnn, vnnn, r, g, b, 0xFF);

    }

    public static void face(VertexConsumer debug, int r, int g, int b, Matrix4f mat, float l) {
        int R = (int)(r * l);
        int G = (int)(g * l);
        int B = (int)(b * l);
        debug.vertex(mat, -1, 1, -1).color(R, G, B, 127).next();
        debug.vertex(mat, 1, 1, -1).color(R, G, B, 127).next();
        debug.vertex(mat, 1, 1, 1).color(R, G, B, 127).next();
        debug.vertex(mat, -1, 1, 1).color(R, G, B, 127).next();
    }

    public static void drawCrosshair(VertexConsumer vc, MatrixStack matrices, double l1, double l2) {
        drawLine(vc, matrices, new Vec3d(l1, 0, 0), new Vec3d(l2, 0, 0), 0xFF, 0, 0, 0xFF);
        drawLine(vc, matrices, new Vec3d(0, l1, 0), new Vec3d(0, l2, 0), 0, 0xFF, 0, 0xFF);
        drawLine(vc, matrices, new Vec3d(0, 0, l1), new Vec3d(0, 0, l2), 0, 0, 0xFF, 0xFF);
        drawLine(vc, matrices, new Vec3d(-l1, 0, 0), new Vec3d(-l2, 0, 0), 0x7F, 0x3F, 0x3F, 0xFF);
        drawLine(vc, matrices, new Vec3d(0, -l1, 0), new Vec3d(0, -l2, 0), 0x3F, 0x7F, 0x3F, 0xFF);
        drawLine(vc, matrices, new Vec3d(0, 0, -l1), new Vec3d(0, 0, -l2), 0x3F, 0x3F, 0x7F, 0xFF);
    }

    public static void render(EditorMainScreen editorMainScreen, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        Vec3d worldOffset = editorMainScreen.originPos;
        Weave weave = editorMainScreen.phySys;

        MinecraftClient.getInstance().getProfiler().push("arachne");
        TimerHolder.dualPush("weave_renderer");

        matrices.push();
        matrices.translate(0.5f, 1.5f, 0.5f);

        TimerHolder.dualPush("faces");
        VertexConsumer vcSolid = vertexConsumers.getBuffer(ModRenderLayers.getSolid());
        ArrayList<Face> faces = weave.faces;
        if(editorMainScreen.renderFaces && faces != null) {
            matrices.push();
            //matrices.translate(- entity.getPos().getX() - 0.5f, - entity.getPos().getY() - 1.5f, - entity.getPos().getZ() - 0.5f);
            for(Face face : faces) {
                if(editorMainScreen.selectionMode == EditorMainScreen.SelectionMode.FACE && editorMainScreen.selection.contains(face)) { // TODO: make not bad
                    int r = face.r;
                    int g = face.g;
                    int b = face.b;
                    face.r = (face.r + 0xFF) / 2;
                    face.g = (face.g + 0xFF) / 2;
                    face.b = (face.b + 0xFF) / 2;
                    renderFace(tickDelta, vcSolid, matrices, face, worldOffset);
                    face.r = r;
                    face.g = g;
                    face.b = b;
                } else {
                    renderFace(tickDelta, vcSolid, matrices, face, worldOffset);
                }
            }
            if(editorMainScreen.toolContainer.tool instanceof FaceCreationTool fct) {
                ArrayList<Node> fctNodes = fct.nodes;
                if(fctNodes.size() >= 3 && fctNodes.get(0) == editorMainScreen.highlightedNode) {
                    Face face = new Face(fctNodes);
                    // TODO: tweak
                    Random random = new Random();
                    int h = 0;
                    for (Node node : fctNodes) {
                        h += node.hashCode();
                    }
                    random.setSeed(h);
                    face.r = random.nextInt(128) + 128;
                    face.g = random.nextInt(128) + 128;
                    face.b = random.nextInt(128) + 128;
                    renderFace(tickDelta, vcSolid, matrices, face, worldOffset);
                }
            }
            matrices.pop();
        }
        TimerHolder.dualPop();

        TimerHolder.dualPush("lines");
        VertexConsumer vcLines = vertexConsumers.getBuffer(RenderLayer.getLines());
        ArrayList<Link> links = editorMainScreen.phySys.links;
        if(editorMainScreen.renderLinks && links != null) {
            matrices.push();
            //matrices.translate(- entity.getPos().getX() - 0.5f, - entity.getPos().getY() - 1.5f, - entity.getPos().getZ() - 0.5f);
            for(Link link : links) {
                double t = 0;
                if(link instanceof StringLink stringLink) {
                    double currentLengthsqr = link.node1.pos.subtract(link.node2.pos).lengthSquared();
                    double idealLengthsqr = stringLink.idealLength * stringLink.idealLength;
                    double currentToIdealSqr = currentLengthsqr / idealLengthsqr;
                    double f = java.lang.Math.log(currentToIdealSqr) / 6;
                    t = Math.clamp(0, 1, f);
                }
                int r = 0xFF;
                int g = (int)(0xBf * (1 - t));
                int b = (int)(0x6F * (1 - t));
                drawLine(vcLines, matrices, link.node1.getPos(tickDelta), link.node2.getPos(tickDelta), r, g, b, 0xFF);
            }
            matrices.pop();
        }


        matrices.push();
        //matrices.translate(- entity.getPos().getX() - 0.5f, - entity.getPos().getY() - 1.5f, - entity.getPos().getZ() - 0.5f);

        if(editorMainScreen.toolContainer.tool instanceof FaceCreationTool fct) { //TODO: tidy
            ArrayList<Node> fctNodes = fct.nodes;
            if(fctNodes.size() >= 2) {
                for(int i = 0; i < fctNodes.size() - 1; i++) {
                    Node n1 = fctNodes.get(i);
                    Node n2 = fctNodes.get(i + 1);
                    drawLine(vcLines, matrices, n1.getPos(tickDelta), n2.getPos(tickDelta), 0xFF, 0x7F, 0x7F, 0xFF);
                }
            }
            Node highlighted = editorMainScreen.highlightedNode;
            if(fctNodes.size() >= 1 && highlighted != null && (!fctNodes.contains(highlighted) || (highlighted == fctNodes.get(0) && fctNodes.size() >= 3))) {
                drawLine(vcLines, matrices, fctNodes.get(fctNodes.size() - 1).getPos(tickDelta), editorMainScreen.highlightedNode.getPos(tickDelta), 0xFF, 0x3f, 0x3F, 0xFF);
            }
        }
        matrices.pop();

        Vec3d targetPos = CameraController.getInstance().targetPos;
        float l = 1/32f;
        matrices.push();
        matrices.translate(-worldOffset.x, -worldOffset.y, -worldOffset.z);
        matrices.translate(targetPos.x, targetPos.y, targetPos.z);
        drawCrosshair(vcLines, matrices, 0, l);
        matrices.pop();
        TimerHolder.dualPop();

        TimerHolder.dualPush("Nodes");
        VertexConsumer circles = vertexConsumers.getBuffer(ModRenderLayers.getDisk());
        Matrix4f posMatrix = matrices.peek().getPositionMatrix();
        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
        Vec3d camLook = CameraController.getCameraLookVector(camera);
        double yaw = Math.toRadians(camera.getYaw());
        double pitch = Math.toRadians(camera.getPitch());
        float sy = (float)Math.sin(yaw);
        float cy = (float)Math.cos(yaw);
        float sp = (float)Math.sin(pitch);
        float cp = (float)Math.cos(pitch);
        for(Node node : editorMainScreen.phySys.nodes) {
            //if(!node.isVisible()) continue;

            Item item = Items.SNOWBALL;
            if(editorMainScreen.toolContainer != null && editorMainScreen.toolContainer.tool instanceof SelectTool selectTool && selectTool.getHighlight() != null) {
                Vec3d min = selectTool.getHighlight().getMinPos();
                Vec3d max = selectTool.getHighlight().getMaxPos();
                if(min.x <= node.posScreenSpace.x && node.posScreenSpace.x <= max.x) {
                    if(min.y <= node.posScreenSpace.y && node.posScreenSpace.y <= max.y) {
                        item = Items.SLIME_BALL;
                    }
                }
            } else {
                if (node == editorMainScreen.highlightedNode) {
                    item = Items.SLIME_BALL;
                }
            }
            if (editorMainScreen.toolContainer != null && editorMainScreen.toolContainer.tool instanceof FaceCreationTool fct) { //TODO: tidy
                if(fct.nodes.contains(node)) {
                    item = Items.REDSTONE;
                }
                if(!fct.nodes.isEmpty() && node == fct.nodes.get(0)) {
                    item = Items.GLOWSTONE_DUST;
                    if (node == editorMainScreen.highlightedNode && fct.nodes.size() >= 3) {
                        item = Items.SLIME_BALL;
                    }
                }
            }
            if (editorMainScreen.selectionMode == EditorMainScreen.SelectionMode.VERTEX && editorMainScreen.selection.contains(node)) {
                item = Items.ENDER_PEARL;
            }
            if(!editorMainScreen.renderNodes && item == Items.SNOWBALL) continue;



            Vec3d pos = node.getPos(tickDelta);
            float x = (float)pos.x;
            float y = (float)pos.y;
            float z = (float)pos.z;
            Vec3d offset = node.pos.add(CameraController.getInstance().originPos).subtract(camera.getPos());
            float d = (float)offset.dotProduct(camLook);
            if(d > 1) {
                d = 1;
            }
            d *= 1/4f;
            float o1x = -sy*sp / 16 * d;
            float o1y = cp / 16 * d;
            float o1z = cy*sp / 16 * d;
            float o2x = -cy / 16 * d;
            float o2y = 0 * d;
            float o2z = -sy / 16 * d;
            int r = 0x3f;
            int g = 0x3f;
            int b = 0x3f;
            if (editorMainScreen.selection.contains(node)) {
                //r = 0xff;
                //g = 0xbf;
                //b = 0x7f;
            }
            if(item == Items.ENDER_PEARL) {
                r = 0xff;
                g = 0xbf;
                b = 0x7f;
            } else if(item == Items.SLIME_BALL) {
                r = 0xff;
                g = 0xff;
                b = 0xff;
            } else if(item == Items.REDSTONE) {
                r = 0xff;
            } else if(item == Items.GLOWSTONE_DUST) {
                r = 0xff;
                g = 0xff;
            }
            circles.vertex(posMatrix, x+o1x+o2x, y+o1y+o2y, z+o1z+o2z).color(r, g, b, 255).texture(0, 0).next();
            circles.vertex(posMatrix, x+o1x-o2x, y+o1y-o2y, z+o1z-o2z).color(r, g, b, 255).texture(1, 0).next();
            circles.vertex(posMatrix, x-o1x-o2x, y-o1y-o2y, z-o1z-o2z).color(r, g, b, 255).texture(1, 1).next();
            circles.vertex(posMatrix, x-o1x+o2x, y-o1y+o2y, z-o1z+o2z).color(r, g, b, 255).texture(0, 1).next();
        }
        TimerHolder.dualPop();


        matrices.pop();

        TimerHolder.dualPop();
        MinecraftClient.getInstance().getProfiler().pop();
    }

    public static void drawLine(VertexConsumer vertexConsumer, MatrixStack matrices, Vec3d point1, Vec3d point2, int r, int g, int b, int a) {
        // TODO: check if should be drawing min to max?
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

    public static void renderFace(float tickDelta, VertexConsumer vertexConsumer, MatrixStack matrices, Face face, Vec3d worldOffset) {
        //TimerHolder.getInstance().push("face"); // TODO: remove timers inside functions like renderFace, they cause noticeable performance drops

        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
        Vec3d avgPos = face.getCenterPos(tickDelta);

        //Function<Identifier, Sprite> ATLAS = MinecraftClient.getInstance().getSpriteAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE);
        //Sprite sprite = ATLAS.apply(new Identifier("minecraft", "block/white_wool"));

        int r = face.r;
        int g = face.g;
        int b = face.b;

        Direction[] directions = new Direction[]{Direction.EAST, Direction.WEST, Direction.UP, Direction.DOWN, Direction.SOUTH, Direction.NORTH};
        float[] directionColors = new float[]{1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f};
        World world = MinecraftClient.getInstance().world;
        if(world != null) {
            for (int i = 0; i < 6; i++) {
                directionColors[i] = world.getBrightness(directions[i], true);
            }
        }

        //TimerHolder.getInstance().push("texture");
        Function<Identifier, Sprite> ATLAS = MinecraftClient.getInstance().getSpriteAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE);
        Sprite sprite;
        if(face.renderMaterial != null && face.renderMaterial.texture != null) {
            sprite = ATLAS.apply(face.renderMaterial.texture);
        } else {
            sprite = ATLAS.apply(MissingSprite.getMissingSpriteId());
        }
        float minU = sprite.getMinU();
        float minV = sprite.getMinV();
        float maxU = sprite.getMaxU();
        float maxV = sprite.getMaxV();
        float lenU = maxU - minU;
        float lenV = maxV - minV;
        float[] ul = new float[face.nodes.length];
        float[] vl = new float[face.nodes.length];
        for(int i = 0; i < face.nodes.length; i++) {
            ul[i] = minU + face.ul[i] * lenU;
            vl[i] = minV + face.vl[i] * lenV;
        }
        float ua = minU + face.getAvgU() * lenU;
        float va = minV + face.getAvgV() * lenV;
        //TimerHolder.getInstance().pop();

        //TimerHolder.getInstance().push("draw");
        Vec3d faceNormal = face.getNormal();
        for(int i = 0; i < face.nodes.length; i++) {
            int j = (i + 1) % face.nodes.length;
            Vec3d p1 = face.nodes[i].getPos(tickDelta);
            float u1 = ul[i];
            float v1 = vl[i];
            Vec3d p2 = face.nodes[j].getPos(tickDelta);
            float u2 = ul[j];
            float v2 = vl[j];
            drawTriangle(vertexConsumer, positionMatrix, p2, avgPos, p1, r, g, b, worldOffset, faceNormal, directionColors, new float[]{u2, v2, ua, va, u1, v1});
            drawTriangle(vertexConsumer, positionMatrix, p1, avgPos, p2, r, g, b, worldOffset, faceNormal.multiply(-1), directionColors, new float[]{u1, v1, ua, va, u2, v2});
        }
        //TimerHolder.getInstance().pop();
        //TimerHolder.getInstance().pop();
    }

    public static void drawTriangle(VertexConsumer vertexConsumer, Matrix4f positionMatrix, Vec3d p1, Vec3d p2, Vec3d p3, int r, int g, int b, Vec3d worldOffset, Vec3d faceNormal, float[] directionColors, float[] uvs) {
        //TimerHolder.getInstance().push("timer_test");
        //TimerHolder.getInstance().pop();

        //TimerHolder.getInstance().push("normals");
        //Vec3d normal = Face.getNormal(p1, p2, p3); // TODO: consider reenabling? this was kinda nice
        //float nx = (float)normal.x;
        //float ny = (float)normal.y;
        //float nz = (float)normal.z;
        float nx = (float)faceNormal.x;
        float ny = (float)faceNormal.y;
        float nz = (float)faceNormal.z;
        //TimerHolder.getInstance().pop();

        //TimerHolder.getInstance().push("light-level");
        // TODO: make lighting work correctly and not just be based on the block its in
        World world = MinecraftClient.getInstance().world;
        int lightLevel = world != null ? WorldRenderer.getLightmapCoordinates(world, new BlockPos((int)Math.floor(p2.x + worldOffset.x), (int)Math.floor(p2.y + worldOffset.y), (int)Math.floor(p2.z + worldOffset.z))) : LightmapTextureManager.MAX_LIGHT_COORDINATE;
        //TimerHolder.getInstance().pop();

        //TimerHolder.getInstance().push("light-amount");
        float l = nx*nx*(nx > 0 ? directionColors[0] : directionColors[1])
                + ny*ny*(ny > 0 ? directionColors[2] : directionColors[3])
                + nz*nz*(nz > 0 ? directionColors[4] : directionColors[5]);
        if(faceNormal.lengthSquared() == 0) { // if overall face normal is 0, set light to 1 // TODO: maybe fix by reverting to separate normals for each sub-face?
            l = 1.0f;
        }
        float rl = r*l/255f;
        float gl = g*l/255f;
        float bl = b*l/255f;
        //TimerHolder.getInstance().pop();

        //TimerHolder.getInstance().push("vertices");
        Vector4f v1 = positionMatrix.transform(new Vector4f((float)p1.x, (float)p1.y, (float)p1.z, 1.0f));
        Vector4f v2 = positionMatrix.transform(new Vector4f((float)p2.x, (float)p2.y, (float)p2.z, 1.0f));
        Vector4f v3 = positionMatrix.transform(new Vector4f((float)p3.x, (float)p3.y, (float)p3.z, 1.0f));
        vertexConsumer.vertex(v1.x, v1.y, v1.z, rl, gl, bl, 255, uvs[0], uvs[1], 0, lightLevel, nx, ny, nz); // TODO: overlay
        vertexConsumer.vertex(v2.x, v2.y, v2.z, rl, gl, bl, 255, uvs[2], uvs[3], 0, lightLevel, nx, ny, nz);
        vertexConsumer.vertex(v3.x, v3.y, v3.z, rl, gl, bl, 255, uvs[4], uvs[5], 0, lightLevel, nx, ny, nz);
        //TimerHolder.getInstance().pop();

    }
}
