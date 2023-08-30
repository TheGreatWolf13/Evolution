package tgw.evolution.client.models.tile;

import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.tileentities.TEFirewoodPile;
import tgw.evolution.client.models.data.IModelData;
import tgw.evolution.client.models.data.ModelProperty;
import tgw.evolution.client.models.data.SimpleModelData;
import tgw.evolution.client.renderer.RenderHelper;
import tgw.evolution.init.EvolutionBStates;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.math.FastRandom;
import tgw.evolution.util.math.IRandom;
import tgw.evolution.util.math.MathHelper;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class BakedModelFirewoodPile implements BakedModel {

    public static final ModelProperty<byte[]> FIREWOOD = new ModelProperty<>();
    private static final byte[] LOG_ORDER;
    private static final byte[] EMPTY_FIREWOOD = new byte[16];
    private static final ThreadLocal<SimpleModelData<byte[]>> MODEL_DATA = ThreadLocal.withInitial(() -> new SimpleModelData<>(FIREWOOD));

    static {
        Arrays.fill(EMPTY_FIREWOOD, (byte) -1);
        byte[] a = new byte[16];
        for (byte i = 0; i < 16; i++) {
            a[i] = i;
        }
        MathHelper.shuffle(a, new FastRandom("Firewood".hashCode()));
        LOG_ORDER = a;
    }

    private final BakedModel baseModel;

    public BakedModelFirewoodPile(BakedModel baseModel) {
        this.baseModel = baseModel;
    }

    private static int getFaceRotation(Direction modelFace, Direction stateFacing, int firewoodIndex) {
        byte realIndex = LOG_ORDER[firewoodIndex];
        int i = realIndex % 4;
        int j = realIndex / 4;
        switch (modelFace) {
            case NORTH -> {
                switch (stateFacing) {
                    case EAST, NORTH -> {
                        return 180;
                    }
                }
                return 0;
            }
            case SOUTH -> {
                switch (stateFacing) {
                    case SOUTH, WEST -> {
                        return 180;
                    }
                }
                return 0;
            }
            case UP -> {
                if (j == 3) {
                    return switch (stateFacing) {
                        case EAST, WEST -> 90;
                        default -> 0;
                    };
                }
                return switch (stateFacing) {
                    case NORTH -> 90;
                    case EAST -> 180;
                    case SOUTH -> 270;
                    default -> 0;
                };
            }
            case DOWN -> {
                if (j == 0) {
                    return switch (stateFacing) {
                        case EAST, WEST -> 90;
                        default -> 180;
                    };
                }
                return switch (stateFacing) {
                    case NORTH -> 90;
                    case WEST -> 180;
                    case SOUTH -> 270;
                    default -> 0;
                };
            }
            case EAST -> {
                if (i == 3) {
                    return switch (stateFacing) {
                        case EAST, NORTH -> 90;
                        default -> 270;
                    };
                }
                return 0;
            }
            case WEST -> {
                if (i == 0) {
                    return switch (stateFacing) {
                        case SOUTH, WEST -> 90;
                        default -> 270;
                    };
                }
                return 0;
            }
        }
        throw new IllegalStateException("Unknown direction: " + modelFace);
    }

    private static BakedQuad getQuadForFirewood(Direction whichFace,
                                                TextureAtlas blockAtlas,
                                                float minX,
                                                float minY,
                                                float minZ,
                                                float maxX,
                                                float maxY,
                                                float maxZ,
                                                int firewoodIndex,
                                                byte variantId,
                                                Direction modelFace,
                                                Direction stateFacing) {
        Vector3f from = RenderHelper.MODEL_FROM.get();
        from.set(minX, minY, minZ);
        Vector3f to = RenderHelper.MODEL_TO.get();
        to.set(maxX, maxY, maxZ);
        BlockFaceUV blockFaceUV = RenderHelper.MODEL_FACE_UV.get();
        blockFaceUV.uvs = getUVs(modelFace, stateFacing, firewoodIndex);
        blockFaceUV.rotation = getFaceRotation(modelFace, stateFacing, firewoodIndex);
        TextureAtlasSprite sprite;
        byte realIndex = LOG_ORDER[firewoodIndex];
        switch (modelFace) {
            case NORTH, SOUTH -> sprite = blockAtlas.getSprite(EvolutionResources.BLOCK_LOG_TOP[variantId]);
            case DOWN -> {
                if (realIndex < 4) {
                    sprite = blockAtlas.getSprite(EvolutionResources.BLOCK_LOG_SIDE[variantId]);
                }
                else {
                    sprite = blockAtlas.getSprite(EvolutionResources.BLOCK_LOG_TOP[variantId]);
                }
            }
            case UP -> {
                if (realIndex >= 12) {
                    sprite = blockAtlas.getSprite(EvolutionResources.BLOCK_LOG_SIDE[variantId]);
                }
                else {
                    sprite = blockAtlas.getSprite(EvolutionResources.BLOCK_LOG_TOP[variantId]);
                }
            }
            case EAST -> {
                if (realIndex % 4 == 3) {
                    sprite = blockAtlas.getSprite(EvolutionResources.BLOCK_LOG_SIDE[variantId]);
                }
                else {
                    sprite = blockAtlas.getSprite(EvolutionResources.BLOCK_LOG_TOP[variantId]);
                }
            }
            case WEST -> {
                if (realIndex % 4 == 0) {
                    sprite = blockAtlas.getSprite(EvolutionResources.BLOCK_LOG_SIDE[variantId]);
                }
                else {
                    sprite = blockAtlas.getSprite(EvolutionResources.BLOCK_LOG_TOP[variantId]);
                }
            }
            default -> throw new IllegalStateException("Unknown direction: " + modelFace);
        }
        return RenderHelper.MODEL_FACE_BAKERY.bakeQuad(from, to, RenderHelper.MODEL_FACE.get(), sprite, whichFace, BlockModelRotation.X0_Y0, null, true, TextureManager.INTENTIONAL_MISSING_TEXTURE);
    }

    private static OList<BakedQuad> getQuadsFromFirewood(Direction stateFacing, int firewoodCount, byte[] firewood, @Nullable Direction side) {
        TextureAtlas blockAtlas = Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS);
        OList<BakedQuad> quads = RenderHelper.MODEL_QUAD_HOLDER.get();
        quads.clear();
        if (side != null) {
            if (stateFacing == side) {
                //Back face -> North
                switch (stateFacing) {
                    case NORTH -> {
                        for (int firewoodIndex = 0; firewoodIndex < firewoodCount; firewoodIndex++) {
                            byte variantIndex = firewood[firewoodIndex];
                            if (variantIndex != -1) {
                                int i = firewoodIndex % 4;
                                int j = firewoodIndex / 4;
                                quads.add(getQuadForFirewood(side, blockAtlas, 4 * i, 4 * j, 0, 4 * i + 4, 4 * j + 4, 16, firewoodIndex, variantIndex, Direction.NORTH, stateFacing));
                            }
                        }
                    }
                    case SOUTH -> {
                        for (int firewoodIndex = 0; firewoodIndex < firewoodCount; firewoodIndex++) {
                            byte variantIndex = firewood[firewoodIndex];
                            if (variantIndex != -1) {
                                int i = firewoodIndex % 4;
                                int j = firewoodIndex / 4;
                                quads.add(getQuadForFirewood(side, blockAtlas, 12 - 4 * i, 4 * j, 0, 16 - 4 * i, 4 * j + 4, 16, firewoodIndex, variantIndex, Direction.NORTH, stateFacing));
                            }
                        }
                    }
                    case WEST -> {
                        for (int firewoodIndex = 0; firewoodIndex < firewoodCount; firewoodIndex++) {
                            byte variantIndex = firewood[firewoodIndex];
                            if (variantIndex != -1) {
                                int i = firewoodIndex % 4;
                                int j = firewoodIndex / 4;
                                quads.add(getQuadForFirewood(side, blockAtlas, 0, 4 * j, 12 - 4 * i, 16, 4 * j + 4, 16 - 4 * i, firewoodIndex, variantIndex, Direction.NORTH, stateFacing));
                            }
                        }
                    }
                    case EAST -> {
                        for (int firewoodIndex = 0; firewoodIndex < firewoodCount; firewoodIndex++) {
                            byte variantIndex = firewood[firewoodIndex];
                            if (variantIndex != -1) {
                                int i = firewoodIndex % 4;
                                int j = firewoodIndex / 4;
                                quads.add(getQuadForFirewood(side, blockAtlas, 0, 4 * j, 4 * i, 16, 4 * j + 4, 4 * i + 4, firewoodIndex, variantIndex, Direction.NORTH, stateFacing));
                            }
                        }
                    }
                }
            }
            else if (stateFacing.getOpposite() == side) {
                //Front face -> South
                switch (stateFacing) {
                    case NORTH -> {
                        for (int firewoodIndex = 0; firewoodIndex < firewoodCount; firewoodIndex++) {
                            byte variantIndex = firewood[firewoodIndex];
                            if (variantIndex != -1) {
                                int i = firewoodIndex % 4;
                                int j = firewoodIndex / 4;
                                quads.add(getQuadForFirewood(side, blockAtlas, 4 * i, 4 * j, 0, 4 * i + 4, 4 * j + 4, 16, firewoodIndex, variantIndex, Direction.SOUTH, stateFacing));
                            }
                        }
                    }
                    case SOUTH -> {
                        for (int firewoodIndex = 0; firewoodIndex < firewoodCount; firewoodIndex++) {
                            byte variantIndex = firewood[firewoodIndex];
                            if (variantIndex != -1) {
                                int i = firewoodIndex % 4;
                                int j = firewoodIndex / 4;
                                quads.add(getQuadForFirewood(side, blockAtlas, 12 - 4 * i, 4 * j, 0, 16 - 4 * i, 4 * j + 4, 16, firewoodIndex, variantIndex, Direction.SOUTH, stateFacing));
                            }
                        }
                    }
                    case WEST -> {
                        for (int firewoodIndex = 0; firewoodIndex < firewoodCount; firewoodIndex++) {
                            byte variantIndex = firewood[firewoodIndex];
                            if (variantIndex != -1) {
                                int i = firewoodIndex % 4;
                                int j = firewoodIndex / 4;
                                quads.add(getQuadForFirewood(side, blockAtlas, 0, 4 * j, 12 - 4 * i, 16, 4 * j + 4, 16 - 4 * i, firewoodIndex, variantIndex, Direction.SOUTH, stateFacing));
                            }
                        }
                    }
                    case EAST -> {
                        for (int firewoodIndex = 0; firewoodIndex < firewoodCount; firewoodIndex++) {
                            byte variantIndex = firewood[firewoodIndex];
                            if (variantIndex != -1) {
                                int i = firewoodIndex % 4;
                                int j = firewoodIndex / 4;
                                quads.add(getQuadForFirewood(side, blockAtlas, 0, 4 * j, 4 * i, 16, 4 * j + 4, 4 * i + 4, firewoodIndex, variantIndex, Direction.SOUTH, stateFacing));
                            }
                        }
                    }
                }
            }
            else if (stateFacing.getClockWise() == side) {
                //Right face -> East
                switch (stateFacing) {
                    case NORTH -> {
                        for (int firewoodIndex = 3; firewoodIndex < firewoodCount; firewoodIndex += 4) {
                            byte variantIndex = firewood[firewoodIndex];
                            if (variantIndex != -1) {
                                int j = firewoodIndex / 4;
                                quads.add(getQuadForFirewood(side, blockAtlas, 12, 4 * j, 0, 16, 4 * j + 4, 16, firewoodIndex, variantIndex, Direction.EAST, stateFacing));
                            }
                        }
                    }
                    case SOUTH -> {
                        for (int firewoodIndex = 3; firewoodIndex < firewoodCount; firewoodIndex += 4) {
                            byte variantIndex = firewood[firewoodIndex];
                            if (variantIndex != -1) {
                                int j = firewoodIndex / 4;
                                quads.add(getQuadForFirewood(side, blockAtlas, 0, 4 * j, 0, 4, 4 * j + 4, 16, firewoodIndex, variantIndex, Direction.EAST, stateFacing));
                            }
                        }
                    }
                    case WEST -> {
                        for (int firewoodIndex = 3; firewoodIndex < firewoodCount; firewoodIndex += 4) {
                            byte variantIndex = firewood[firewoodIndex];
                            if (variantIndex != -1) {
                                int j = firewoodIndex / 4;
                                quads.add(getQuadForFirewood(side, blockAtlas, 0, 4 * j, 0, 16, 4 * j + 4, 4, firewoodIndex, variantIndex, Direction.EAST, stateFacing));
                            }
                        }
                    }
                    case EAST -> {
                        for (int firewoodIndex = 3; firewoodIndex < firewoodCount; firewoodIndex += 4) {
                            byte variantIndex = firewood[firewoodIndex];
                            if (variantIndex != -1) {
                                int j = firewoodIndex / 4;
                                quads.add(getQuadForFirewood(side, blockAtlas, 0, 4 * j, 12, 16, 4 * j + 4, 16, firewoodIndex, variantIndex, Direction.EAST, stateFacing));
                            }
                        }
                    }
                }
            }
            else if (stateFacing.getCounterClockWise() == side) {
                //Left face -> West
                switch (stateFacing) {
                    case NORTH -> {
                        for (int firewoodIndex = 0; firewoodIndex < firewoodCount; firewoodIndex += 4) {
                            byte variantIndex = firewood[firewoodIndex];
                            if (variantIndex != -1) {
                                int j = firewoodIndex / 4;
                                quads.add(getQuadForFirewood(side, blockAtlas, 0, 4 * j, 0, 4, 4 * j + 4, 16, firewoodIndex, variantIndex, Direction.WEST, stateFacing));
                            }
                        }
                    }
                    case SOUTH -> {
                        for (int firewoodIndex = 0; firewoodIndex < firewoodCount; firewoodIndex += 4) {
                            byte variantIndex = firewood[firewoodIndex];
                            if (variantIndex != -1) {
                                int j = firewoodIndex / 4;
                                quads.add(getQuadForFirewood(side, blockAtlas, 12, 4 * j, 0, 16, 4 * j + 4, 16, firewoodIndex, variantIndex, Direction.WEST, stateFacing));
                            }
                        }
                    }
                    case WEST -> {
                        for (int firewoodIndex = 0; firewoodIndex < firewoodCount; firewoodIndex += 4) {
                            byte variantIndex = firewood[firewoodIndex];
                            if (variantIndex != -1) {
                                int j = firewoodIndex / 4;
                                quads.add(getQuadForFirewood(side, blockAtlas, 0, 4 * j, 12, 16, 4 * j + 4, 16, firewoodIndex, variantIndex, Direction.WEST, stateFacing));
                            }
                        }
                    }
                    case EAST -> {
                        for (int firewoodIndex = 0; firewoodIndex < firewoodCount; firewoodIndex += 4) {
                            byte variantIndex = firewood[firewoodIndex];
                            if (variantIndex != -1) {
                                int j = firewoodIndex / 4;
                                quads.add(getQuadForFirewood(side, blockAtlas, 0, 4 * j, 0, 16, 4 * j + 4, 4, firewoodIndex, variantIndex, Direction.WEST, stateFacing));
                            }
                        }
                    }
                }
            }
            else if (side == Direction.UP) {
                //Top face -> Up
                switch (stateFacing) {
                    case NORTH -> {
                        for (int firewoodIndex = 12; firewoodIndex < firewoodCount; firewoodIndex++) {
                            byte variantIndex = firewood[firewoodIndex];
                            if (variantIndex != -1) {
                                int i = firewoodIndex % 4;
                                quads.add(getQuadForFirewood(side, blockAtlas, 4 * i, 12, 0, 4 * i + 4, 16, 16, firewoodIndex, variantIndex, Direction.UP, stateFacing));
                            }
                        }
                    }
                    case SOUTH -> {
                        for (int firewoodIndex = 12; firewoodIndex < firewoodCount; firewoodIndex++) {
                            byte variantIndex = firewood[firewoodIndex];
                            if (variantIndex != -1) {
                                int i = firewoodIndex % 4;
                                quads.add(getQuadForFirewood(side, blockAtlas, 12 - 4 * i, 12, 0, 16 - 4 * i, 16, 16, firewoodIndex, variantIndex, Direction.UP, stateFacing));
                            }
                        }
                    }
                    case WEST -> {
                        for (int firewoodIndex = 12; firewoodIndex < firewoodCount; firewoodIndex++) {
                            byte variantIndex = firewood[firewoodIndex];
                            if (variantIndex != -1) {
                                int i = firewoodIndex % 4;
                                quads.add(getQuadForFirewood(side, blockAtlas, 0, 12, 12 - 4 * i, 16, 16, 16 - 4 * i, firewoodIndex, variantIndex, Direction.UP, stateFacing));
                            }
                        }
                    }
                    case EAST -> {
                        for (int firewoodIndex = 12; firewoodIndex < firewoodCount; firewoodIndex++) {
                            byte variantIndex = firewood[firewoodIndex];
                            if (variantIndex != -1) {
                                int i = firewoodIndex % 4;
                                quads.add(getQuadForFirewood(side, blockAtlas, 0, 12, 4 * i, 16, 16, 4 * i + 4, firewoodIndex, variantIndex, Direction.UP, stateFacing));
                            }
                        }
                    }
                }
            }
            else {
                //Bottom face -> Down
                switch (stateFacing) {
                    case NORTH -> {
                        for (int firewoodIndex = 0; firewoodIndex < firewoodCount && firewoodIndex < 4; firewoodIndex++) {
                            byte variantIndex = firewood[firewoodIndex];
                            if (variantIndex != -1) {
                                int i = firewoodIndex % 4;
                                quads.add(getQuadForFirewood(side, blockAtlas, 4 * i, 0, 0, 4 * i + 4, 4, 16, firewoodIndex, variantIndex, Direction.DOWN, stateFacing));
                            }
                        }
                    }
                    case SOUTH -> {
                        for (int firewoodIndex = 0; firewoodIndex < firewoodCount && firewoodIndex < 4; firewoodIndex++) {
                            byte variantIndex = firewood[firewoodIndex];
                            if (variantIndex != -1) {
                                int i = firewoodIndex % 4;
                                quads.add(getQuadForFirewood(side, blockAtlas, 12 - 4 * i, 0, 0, 16 - 4 * i, 4, 16, firewoodIndex, variantIndex, Direction.DOWN, stateFacing));
                            }
                        }
                    }
                    case WEST -> {
                        for (int firewoodIndex = 0; firewoodIndex < firewoodCount && firewoodIndex < 4; firewoodIndex++) {
                            byte variantIndex = firewood[firewoodIndex];
                            if (variantIndex != -1) {
                                int i = firewoodIndex % 4;
                                quads.add(getQuadForFirewood(side, blockAtlas, 0, 0, 12 - 4 * i, 16, 4, 16 - 4 * i, firewoodIndex, variantIndex, Direction.DOWN, stateFacing));
                            }
                        }
                    }
                    case EAST -> {
                        for (int firewoodIndex = 0; firewoodIndex < firewoodCount && firewoodIndex < 4; firewoodIndex++) {
                            byte variantIndex = firewood[firewoodIndex];
                            if (variantIndex != -1) {
                                int i = firewoodIndex % 4;
                                quads.add(getQuadForFirewood(side, blockAtlas, 0, 0, 4 * i, 16, 4, 4 * i + 4, firewoodIndex, variantIndex, Direction.DOWN, stateFacing));
                            }
                        }
                    }
                }
            }
        }
        else {
            switch (stateFacing) {
                case NORTH -> {
                    for (int firewoodIndex = 0; firewoodIndex < firewoodCount; firewoodIndex++) {
                        byte variantIndex = firewood[firewoodIndex];
                        if (variantIndex != -1) {
                            int i = firewoodIndex % 4;
                            int j = firewoodIndex / 4;
                            //Up
                            if (j != 3 && firewood[firewoodIndex + 4] == -1) {
                                quads.add(getQuadForFirewood(Direction.UP, blockAtlas, 4 * i, 4 * j, 0, 4 * i + 4, 4 * j + 4, 16, firewoodIndex, variantIndex, Direction.UP, stateFacing));
                            }
                            //Right
                            if (i != 3 && firewood[firewoodIndex + 1] == -1) {
                                quads.add(getQuadForFirewood(Direction.EAST, blockAtlas, 4 * i, 4 * j, 0, 4 * i + 4, 4 * j + 4, 16, firewoodIndex, variantIndex, Direction.EAST, stateFacing));
                            }
                        }
                    }
                }
                case SOUTH -> {
                    for (int firewoodIndex = 0; firewoodIndex < firewoodCount; firewoodIndex++) {
                        byte variantIndex = firewood[firewoodIndex];
                        if (variantIndex != -1) {
                            int i = firewoodIndex % 4;
                            int j = firewoodIndex / 4;
                            //Up
                            if (j != 3 && firewood[firewoodIndex + 4] == -1) {
                                quads.add(getQuadForFirewood(Direction.UP, blockAtlas, 12 - 4 * i, 4 * j, 0, 16 - 4 * i, 4 * j + 4, 16, firewoodIndex, variantIndex, Direction.UP, stateFacing));
                            }
                            //Right
                            if (i != 3 && firewood[firewoodIndex + 1] == -1) {
                                quads.add(getQuadForFirewood(Direction.WEST, blockAtlas, 12 - 4 * i, 4 * j, 0, 16 - 4 * i, 4 * j + 4, 16, firewoodIndex, variantIndex, Direction.EAST, stateFacing));
                            }
                        }
                    }
                }
                case EAST -> {
                    for (int firewoodIndex = 0; firewoodIndex < firewoodCount; firewoodIndex++) {
                        byte variantIndex = firewood[firewoodIndex];
                        if (variantIndex != -1) {
                            int i = firewoodIndex % 4;
                            int j = firewoodIndex / 4;
                            //Up
                            if (j != 3 && firewood[firewoodIndex + 4] == -1) {
                                quads.add(getQuadForFirewood(Direction.UP, blockAtlas, 0, 4 * j, 4 * i, 16, 4 * j + 4, 4 * i + 4, firewoodIndex, variantIndex, Direction.UP, stateFacing));
                            }
                            //Right
                            if (i != 3 && firewood[firewoodIndex + 1] == -1) {
                                quads.add(getQuadForFirewood(Direction.SOUTH, blockAtlas, 0, 4 * j, 4 * i, 16, 4 * j + 4, 4 * i + 4, firewoodIndex, variantIndex, Direction.EAST, stateFacing));
                            }
                        }
                    }
                }
                case WEST -> {
                    for (int firewoodIndex = 0; firewoodIndex < firewoodCount; firewoodIndex++) {
                        byte variantIndex = firewood[firewoodIndex];
                        if (variantIndex != -1) {
                            int i = firewoodIndex % 4;
                            int j = firewoodIndex / 4;
                            //Up
                            if (j != 3 && firewood[firewoodIndex + 4] == -1) {
                                quads.add(getQuadForFirewood(Direction.UP, blockAtlas, 0, 4 * j, 12 - 4 * i, 16, 4 * j + 4, 16 - 4 * i, firewoodIndex, variantIndex, Direction.UP, stateFacing));
                            }
                            //Right
                            if (i != 3 && firewood[firewoodIndex + 1] == -1) {
                                quads.add(getQuadForFirewood(Direction.NORTH, blockAtlas, 0, 4 * j, 12 - 4 * i, 16, 4 * j + 4, 16 - 4 * i, firewoodIndex, variantIndex, Direction.EAST, stateFacing));
                            }
                        }
                    }
                }
            }
        }
        return quads;
    }

    private static float[] getUVs(Direction modelFace, Direction stateFacing, int firewoodIndex) {
        byte realIndex = LOG_ORDER[firewoodIndex];
        int i = realIndex % 4;
        int j = realIndex / 4;
        float[] uv = RenderHelper.MODEL_UV.get();
        switch (modelFace) {
            case SOUTH -> {
                switch (stateFacing) {
                    case SOUTH, WEST -> {
                        uv[0] = 12 - 4 * i;
                        uv[1] = 4 * j;
                        uv[2] = 16 - 4 * i;
                        uv[3] = 4 * j + 4;
                        return uv;
                    }
                }
                uv[0] = 4 * i;
                uv[1] = 12 - 4 * j;
                uv[2] = 4 * i + 4;
                uv[3] = 16 - 4 * j;
                return uv;
            }
            case NORTH -> {
                switch (stateFacing) {
                    case EAST, NORTH -> {
                        uv[0] = 4 * i;
                        uv[1] = 4 * j;
                        uv[2] = 4 * i + 4;
                        uv[3] = 4 * j + 4;
                        return uv;
                    }
                }
                uv[0] = 12 - 4 * i;
                uv[1] = 12 - 4 * j;
                uv[2] = 16 - 4 * i;
                uv[3] = 16 - 4 * j;
                return uv;
            }
            case UP -> {
                if (j == 3) {
                    switch (stateFacing) {
                        case SOUTH, WEST -> {
                            uv[0] = 12 - 4 * i;
                            uv[1] = 0;
                            uv[2] = 16 - 4 * i;
                            uv[3] = 16;
                            return uv;
                        }
                    }
                    uv[0] = 4 * i;
                    uv[1] = 0;
                    uv[2] = 4 * i + 4;
                    uv[3] = 16;
                    return uv;
                }
                uv[0] = 7;
                uv[1] = 12 - 4 * i;
                uv[2] = 9;
                uv[3] = 16 - 4 * i;
                return uv;
            }
            case DOWN -> {
                if (j == 0) {
                    switch (stateFacing) {
                        case SOUTH, WEST -> {
                            uv[0] = 4 * i;
                            uv[1] = 0;
                            uv[2] = 4 * i + 4;
                            uv[3] = 16;
                            return uv;
                        }
                    }
                    uv[0] = 12 - 4 * i;
                    uv[1] = 0;
                    uv[2] = 16 - 4 * i;
                    uv[3] = 16;
                    return uv;
                }
                uv[0] = 7;
                uv[1] = 12 - 4 * i;
                uv[2] = 9;
                uv[3] = 16 - 4 * i;
                return uv;
            }
            case EAST -> {
                if (i == 3) {
                    switch (stateFacing) {
                        case EAST, NORTH -> {
                            uv[0] = 12 - 4 * j;
                            uv[1] = 0;
                            uv[2] = 16 - 4 * j;
                            uv[3] = 16;
                            return uv;
                        }
                    }
                    uv[0] = 4 * j;
                    uv[1] = 0;
                    uv[2] = 4 * j + 4;
                    uv[3] = 16;
                    return uv;
                }
                uv[0] = 7;
                uv[1] = 12 - 4 * j;
                uv[2] = 9;
                uv[3] = 16 - 4 * j;
                return uv;
            }
            case WEST -> {
                if (i == 0) {
                    switch (stateFacing) {
                        case SOUTH, WEST -> {
                            uv[0] = 12 - 4 * j;
                            uv[1] = 0;
                            uv[2] = 16 - 4 * j;
                            uv[3] = 16;
                            return uv;
                        }
                    }
                    uv[0] = 4 * j;
                    uv[1] = 0;
                    uv[2] = 4 * j + 4;
                    uv[3] = 16;
                    return uv;
                }
                uv[0] = 7;
                uv[1] = 12 - 4 * j;
                uv[2] = 9;
                uv[3] = 16 - 4 * j;
                return uv;
            }
        }
        throw new IllegalStateException("Unknown direction: " + modelFace);
    }

    private OList<BakedQuad> getBakedQuadsFromIModelData(@Nullable BlockState state, @Nullable Direction side, IRandom rand, IModelData data) {
        if (!data.hasProperty(FIREWOOD) || state == null) {
            Evolution.error("IModelData did not have expected property FIREWOOD");
            return this.baseModel.getQuads(state, side, rand, data);
        }
        byte[] firewood = data.getData(FIREWOOD);
        assert firewood != null;
        return getQuadsFromFirewood(state.getValue(EvolutionBStates.DIRECTION_HORIZONTAL), state.getValue(EvolutionBStates.FIREWOOD_COUNT), firewood, side).view();
    }

    @Override
    public @NotNull IModelData getModelData(BlockAndTintGetter level, int x, int y, int z, BlockState state) {
        BlockEntity tile = level.getBlockEntity_(x, y, z);
        SimpleModelData<byte[]> modelDataMap = MODEL_DATA.get();
        if (tile instanceof TEFirewoodPile teFirewoodPile) {
            modelDataMap.setData(FIREWOOD, teFirewoodPile.getFirewood());
        }
        else {
            modelDataMap.setData(FIREWOOD, EMPTY_FIREWOOD);
        }
        return modelDataMap;
    }

    @Override
    public ItemOverrides getOverrides() {
        return this.baseModel.getOverrides();
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return this.baseModel.getParticleIcon();
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, Random random) {
        throw new AssertionError("IBakedModel::getQuads should never be called, only PatchBakedModel::getQuads");
    }

    @Override
    public @NotNull OList<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, IRandom rand, IModelData extraData) {
        return this.getBakedQuadsFromIModelData(state, side, rand, extraData);
    }

    @Override
    public ItemTransforms getTransforms() {
        return ItemTransforms.NO_TRANSFORMS;
    }

    @Override
    public boolean isCustomRenderer() {
        return this.baseModel.isCustomRenderer();
    }

    @Override
    public boolean isGui3d() {
        return this.baseModel.isGui3d();
    }

    @Override
    public boolean useAmbientOcclusion() {
        return this.baseModel.useAmbientOcclusion();
    }

    @Override
    public boolean usesBlockLight() {
        return this.baseModel.usesBlockLight();
    }
}
