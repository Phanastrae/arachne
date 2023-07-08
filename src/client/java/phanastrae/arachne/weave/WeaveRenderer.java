package phanastrae.arachne.weave;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Math;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import phanastrae.arachne.CameraController;
import phanastrae.arachne.ModRenderLayers;
import phanastrae.arachne.screen.editor.EditorMainScreen;
import phanastrae.arachne.weave.link_type.Link;
import phanastrae.arachne.weave.link_type.StringLink;
import phanastrae.arachne.screen.editor.tools.FaceCreationTool;
import phanastrae.arachne.screen.editor.tools.SelectTool;
import phanastrae.arachne.util.TimerHolder;

import java.util.ArrayList;
import java.util.Random;
import java.util.function.Function;

public class WeaveRenderer {

    public static void renderEntityWeaves(Entity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        WeaveControl.forEachWeaveInEntity(entity, (string, weaveCache) -> renderEntityWeave(entity, weaveCache.getWeave(), yaw, tickDelta, matrices, vertexConsumers, light));
    }

    public static void renderEntityWeave(Entity entity, @Nullable Weave weave, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        if(weave == null) return;

        if(weave instanceof PhysicsSystem physicsSystem) {
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
        if(weave instanceof PhysicsSystem physicsSystem && physicsSystem.lastPos != null) {
            worldOffset = physicsSystem.lastPos;
        }
        MinecraftClient.getInstance().getProfiler().push("arachne");
        TimerHolder.dualPush("weave_renderer");
        TimerHolder.dualPush("faces");
        for(Face face : weave.faces) {
            renderFace(tickDelta, vcSolid, matrices, face, worldOffset);
        }
        TimerHolder.dualPop();
        TimerHolder.dualPop();
        MinecraftClient.getInstance().getProfiler().pop();
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
                renderLine(vcLines, matrices, link.node1.getPos(tickDelta), link.node2.getPos(tickDelta), r, g, b, 0xFF);
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
                    renderLine(vcLines, matrices, n1.getPos(tickDelta), n2.getPos(tickDelta), 0xFF, 0x7F, 0x7F, 0xFF);
                }
            }
            Node highlighted = editorMainScreen.highlightedNode;
            if(fctNodes.size() >= 1 && highlighted != null && (!fctNodes.contains(highlighted) || (highlighted == fctNodes.get(0) && fctNodes.size() >= 3))) {
                renderLine(vcLines, matrices, fctNodes.get(fctNodes.size() - 1).getPos(tickDelta), editorMainScreen.highlightedNode.getPos(tickDelta), 0xFF, 0x3f, 0x3F, 0xFF);
            }
        }
        matrices.pop();

        Vec3d targetPos = CameraController.getInstance().targetPos;
        float l = 1/32f;
        matrices.push();
        matrices.translate(-worldOffset.x, -worldOffset.y, -worldOffset.z);
        renderLine(vcLines, matrices, targetPos.add(l, 0, 0), targetPos.add(-l, 0, 0), 0xFF, 0, 0, 0xFF);
        renderLine(vcLines, matrices, targetPos.add(0, l, 0), targetPos.add(0, -l, 0), 0, 0xFF, 0, 0xFF);
        renderLine(vcLines, matrices, targetPos.add(0, 0, l), targetPos.add(0, 0, -l), 0, 0, 0xFF, 0xFF);
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

    public static void renderLine(VertexConsumer vertexConsumer, MatrixStack matrices, Vec3d point1, Vec3d point2, int r, int g, int b, int a) {
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
