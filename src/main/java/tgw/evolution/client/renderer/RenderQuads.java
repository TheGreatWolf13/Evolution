package tgw.evolution.client.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;
import tgw.evolution.blocks.tileentities.TEKnapping;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.util.Vec2f;

public final class RenderQuads {

    private static final Vector3f BOTTOM_LEFT_POS = new Vector3f();
    private static final Vector3f BOTTOM_RIGHT_POS = new Vector3f();
    private static final Vector3f TOP_RIGHT_POS = new Vector3f();
    private static final Vector3f TOP_LEFT_POS = new Vector3f();
    private static final Vec2f BOTTOM_LEFT_UV_POS = new Vec2f();
    private static final Vec2f BOTTOM_RIGHT_UV_POS = new Vec2f();
    private static final Vec2f TOP_LEFT_UV_POS = new Vec2f();
    private static final Vec2f TOP_RIGHT_UV_POS = new Vec2f();
    private static final BlockPos.Mutable POS = new BlockPos.Mutable();

    private RenderQuads() {
    }

    private static void addFace(Direction whichFace,
                                Matrix4f matrixPos,
                                Matrix3f matrixNormal,
                                IVertexBuilder renderBuffer,
                                int r,
                                int g,
                                int b,
                                int a,
                                float centreX,
                                float centreY,
                                float centreZ,
                                float width,
                                float height,
                                float leftUV,
                                float topUV,
                                int lightmapValue) {
        // the Direction class has a bunch of methods which can help you rotate quads
        //  I've written the calculations out long hand, and based them on a centre position, to make it clearer what
        //   is going on.
        // Beware that the Direction class is based on which direction the face is pointing, which is opposite to
        //   the direction that the viewer is facing when looking at the face.
        // Eg when drawing the NORTH face, the face points north, but when we're looking at the face, we are facing south,
        //   so that the bottom left corner is the eastern-most, not the western-most!
        // calculate the bottom left, bottom right, top right, top left vertices from the VIEWER's point of view (not the
        //  face's point of view)
        Vector3f leftToRightDirection;
        Vector3f bottomToTopDirection;
        switch (whichFace) {
            case NORTH: {
                leftToRightDirection = Vector3f.XN;
                bottomToTopDirection = Vector3f.YP;
                break;
            }
            case SOUTH: {
                leftToRightDirection = Vector3f.XP;
                bottomToTopDirection = Vector3f.YP;
                break;
            }
            case EAST: {
                leftToRightDirection = Vector3f.ZN;
                bottomToTopDirection = Vector3f.YP;
                break;
            }
            case WEST: {
                leftToRightDirection = Vector3f.ZP;
                bottomToTopDirection = Vector3f.YP;
                break;
            }
            case UP: { // bottom left is southwest by minecraft block convention
                leftToRightDirection = Vector3f.XN;
                bottomToTopDirection = Vector3f.ZP;
                break;
            }
            case DOWN: { // bottom left is northwest by minecraft block convention
                leftToRightDirection = Vector3f.XP;
                bottomToTopDirection = Vector3f.ZP;
                break;
            }
            default: {
                throw new IllegalStateException("Unknown Direction: " + whichFace);
            }
        }
        // calculate the four vertices based on the centre of the face
        float leftX = leftToRightDirection.x() * width * 0.5f;
        float leftY = leftToRightDirection.y() * width * 0.5f;
        float leftZ = leftToRightDirection.z() * width * 0.5f;
        float bottomX = bottomToTopDirection.x() * height * 0.5f;
        float bottomY = bottomToTopDirection.y() * height * 0.5f;
        float bottomZ = bottomToTopDirection.z() * height * 0.5f;
        BOTTOM_LEFT_POS.set(centreX, centreY, centreZ);
        BOTTOM_LEFT_POS.add(-leftX, -leftY, -leftZ);
        BOTTOM_LEFT_POS.add(-bottomX, -bottomY, -bottomZ);
        BOTTOM_RIGHT_POS.set(centreX, centreY, centreZ);
        BOTTOM_RIGHT_POS.add(leftX, leftY, leftZ);
        BOTTOM_RIGHT_POS.add(-bottomX, -bottomY, -bottomZ);
        TOP_RIGHT_POS.set(centreX, centreY, centreZ);
        TOP_RIGHT_POS.add(leftX, leftY, leftZ);
        TOP_RIGHT_POS.add(bottomX, bottomY, bottomZ);
        TOP_LEFT_POS.set(centreX, centreY, centreZ);
        TOP_LEFT_POS.add(-leftX, -leftY, -leftZ);
        TOP_LEFT_POS.add(bottomX, bottomY, bottomZ);
        // texture coordinates are "upside down" relative to the face
        // eg bottom left = [U min, V max]
        BOTTOM_LEFT_UV_POS.set(leftUV + width, topUV);
        BOTTOM_RIGHT_UV_POS.set(leftUV, topUV);
        TOP_RIGHT_UV_POS.set(leftUV, topUV + height);
        TOP_LEFT_UV_POS.set(leftUV + width, topUV + height);
        if (whichFace.get2DDataValue() != -1) {
            addQuad(matrixPos,
                    matrixNormal,
                    renderBuffer,
                    BOTTOM_LEFT_POS,
                    BOTTOM_RIGHT_POS,
                    TOP_RIGHT_POS,
                    TOP_LEFT_POS,
                    TOP_RIGHT_UV_POS,
                    TOP_LEFT_UV_POS,
                    BOTTOM_LEFT_UV_POS,
                    BOTTOM_RIGHT_UV_POS,
                    whichFace.getStepX(),
                    whichFace.getStepY(),
                    whichFace.getStepZ(),
                    r,
                    g,
                    b,
                    a,
                    lightmapValue);
        }
        else {
            addQuad(matrixPos,
                    matrixNormal,
                    renderBuffer,
                    BOTTOM_LEFT_POS,
                    BOTTOM_RIGHT_POS,
                    TOP_RIGHT_POS,
                    TOP_LEFT_POS,
                    BOTTOM_LEFT_UV_POS,
                    BOTTOM_RIGHT_UV_POS,
                    TOP_RIGHT_UV_POS,
                    TOP_LEFT_UV_POS,
                    whichFace.getStepX(),
                    whichFace.getStepY(),
                    whichFace.getStepZ(),
                    r,
                    g,
                    b,
                    a,
                    lightmapValue);
        }
    }

    /**
     * Add a quad.
     * The vertices are added in anti-clockwise order from the VIEWER's  point of view, i.e.
     * bottom left; bottom right, top right, top left
     * If you add the vertices in clockwise order, the quad will face in the opposite direction; i.e. the viewer will be
     * looking at the back face, which is usually culled (not visible)
     * See
     * http://greyminecraftcoder.blogspot.com/2014/12/the-tessellator-and-worldrenderer-18.html
     * http://greyminecraftcoder.blogspot.com/2014/12/block-models-texturing-quads-faces.html
     */
    private static void addQuad(Matrix4f matrixPos,
                                Matrix3f matrixNormal,
                                IVertexBuilder renderBuffer,
                                Vector3f blpos,
                                Vector3f brpos,
                                Vector3f trpos,
                                Vector3f tlpos,
                                Vec2f blUVpos,
                                Vec2f brUVpos,
                                Vec2f trUVpos,
                                Vec2f tlUVpos,
                                float normalX,
                                float normalY,
                                float normalZ,
                                int r,
                                int g,
                                int b,
                                int a,
                                int lightmapValue) {
        addQuadVertex(matrixPos, matrixNormal, renderBuffer, blpos, blUVpos, normalX, normalY, normalZ, r, g, b, a, lightmapValue);
        addQuadVertex(matrixPos, matrixNormal, renderBuffer, brpos, brUVpos, normalX, normalY, normalZ, r, g, b, a, lightmapValue);
        addQuadVertex(matrixPos, matrixNormal, renderBuffer, trpos, trUVpos, normalX, normalY, normalZ, r, g, b, a, lightmapValue);
        addQuadVertex(matrixPos, matrixNormal, renderBuffer, tlpos, tlUVpos, normalX, normalY, normalZ, r, g, b, a, lightmapValue);
    }

    private static void addQuadVertex(Matrix4f matrixPos,
                                      Matrix3f matrixNormal,
                                      IVertexBuilder renderBuffer,
                                      Vector3f pos,
                                      Vec2f texUV,
                                      float normalX,
                                      float normalY,
                                      float normalZ,
                                      int r,
                                      int g,
                                      int b,
                                      int a,
                                      int lightmapValue) {
        renderBuffer.vertex(matrixPos, pos.x(), pos.y(), pos.z())                 // position coordinate
                    .color(r, g, b, a)                                            // color
                    .uv(texUV.getX(), texUV.getY())                               // texel coordinate
                    .overlayCoords(OverlayTexture.NO_OVERLAY)                     // only relevant for rendering Entities (Living)
                    .uv2(lightmapValue)                                           // lightmap with full brightness
                    .normal(matrixNormal, normalX, normalY, normalZ).endVertex();
    }

    public static void drawKnapping(TEKnapping tile, int id, MatrixStack matrices, IRenderTypeBuffer renderBuffer, int color, int combinedLight) {
        IVertexBuilder vertexBuilderBlockQuads = renderBuffer.getBuffer(RenderType.entitySolid(EvolutionResources.BLOCK_KNAPPING[id]));
        // other typical RenderTypes used by TER are:
        // getEntityCutout, getBeaconBeam (which has translucency),
        Matrix4f matrixPos = matrices.last().pose();
        Matrix3f matrixNormal = matrices.last().normal();
        // all faces have the same height and width
        float horizWidth = 1 / 8.0f;
        float horizHeight = 1 / 8.0f;
        final float width = 1 / 8.0f;
        final float height = 1 / 16.0f;
        World world = tile.getLevel();
        BlockPos pos = tile.getBlockPos();
        boolean solidDown = world.getBlockState(POS.setWithOffset(pos, Direction.DOWN)).canOcclude();
        boolean solidNorth = world.getBlockState(POS.setWithOffset(pos, Direction.NORTH)).canOcclude();
        boolean solidSouth = world.getBlockState(POS.setWithOffset(pos, Direction.SOUTH)).canOcclude();
        boolean solidEast = world.getBlockState(POS.setWithOffset(pos, Direction.EAST)).canOcclude();
        boolean solidWest = world.getBlockState(POS.setWithOffset(pos, Direction.WEST)).canOcclude();
        int a = color >> 24 & 0xff;
        int r = color >> 16 & 0xff;
        int g = color >> 8 & 0xff;
        int b = color & 0xff;
        for (int j = 0; j < 8; j++) {
            for (int i = 0; i < 8; i++) {
                if (tile.getPart(i, j)) {
                    float centreX = 1 / 16.0f + 1 / 8.0f * i;
                    float centreZ = 1 / 16.0f + 1 / 8.0f * j;
                    float leftX = 1 / 8.0f * i;
                    float topZ = 1 / 8.0f * j;
                    addFace(Direction.UP,
                            matrixPos,
                            matrixNormal,
                            vertexBuilderBlockQuads,
                            r,
                            g,
                            b,
                            a,
                            centreX,
                            1 / 16.0f,
                            centreZ,
                            horizWidth,
                            horizHeight,
                            leftX,
                            topZ,
                            combinedLight);
                    if (!solidDown) {
                        addFace(Direction.DOWN,
                                matrixPos,
                                matrixNormal,
                                vertexBuilderBlockQuads,
                                r,
                                g,
                                b,
                                a,
                                centreX,
                                0.0f,
                                centreZ,
                                horizWidth,
                                horizHeight,
                                leftX,
                                topZ,
                                combinedLight);
                    }
                    if (j == 0 && !solidNorth || j > 0 && !tile.getPart(i, j - 1)) {
                        addFace(Direction.NORTH,
                                matrixPos,
                                matrixNormal,
                                vertexBuilderBlockQuads,
                                r,
                                g,
                                b,
                                a,
                                centreX,
                                1 / 32.0f,
                                topZ,
                                width,
                                height,
                                7 / 8.0f - leftX,
                                15 / 16.0f,
                                combinedLight);
                    }
                    if (j == 7 && !solidSouth || j < 7 && !tile.getPart(i, j + 1)) {
                        addFace(Direction.SOUTH,
                                matrixPos,
                                matrixNormal,
                                vertexBuilderBlockQuads,
                                r,
                                g,
                                b,
                                a,
                                centreX,
                                1 / 32.0f,
                                topZ + 1 / 8.0f,
                                width,
                                height,
                                leftX,
                                15 / 16.0f,
                                combinedLight);
                    }
                    if (i == 0 && !solidWest || i > 0 && !tile.getPart(i - 1, j)) {
                        addFace(Direction.WEST,
                                matrixPos,
                                matrixNormal,
                                vertexBuilderBlockQuads,
                                r,
                                g,
                                b,
                                a,
                                leftX,
                                1 / 32.0f,
                                centreZ,
                                width,
                                height,
                                topZ,
                                15 / 16.0f,
                                combinedLight);
                    }
                    if (i == 7 && !solidEast || i < 7 && !tile.getPart(i + 1, j)) {
                        addFace(Direction.EAST,
                                matrixPos,
                                matrixNormal,
                                vertexBuilderBlockQuads,
                                r,
                                g,
                                b,
                                a,
                                leftX + 1 / 8.0f,
                                1 / 32.0f,
                                centreZ,
                                width,
                                height,
                                7 / 8.0f - topZ,
                                15 / 16.0f,
                                combinedLight);
                    }
                }
            }
        }
    }
}
