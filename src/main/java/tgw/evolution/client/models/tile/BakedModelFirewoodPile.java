package tgw.evolution.client.models.tile;

import com.google.common.collect.ImmutableList;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.ForgeModelBakery;
import net.minecraftforge.client.model.SimpleModelState;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.tileentities.TEFirewoodPile;
import tgw.evolution.init.EvolutionBStates;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.util.collection.BArrayList;
import tgw.evolution.util.collection.BList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class BakedModelFirewoodPile implements BakedModel {

    public static final ModelProperty<byte[]> FIREWOOD = new ModelProperty<>();
    private static final byte[] EMPTY_FIREWOOD = new byte[16];
    private static final Vector3f FROM = new Vector3f();
    private static final Vector3f TO = new Vector3f();
    private static final FaceBakery FACE_BAKERY = new FaceBakery();
    private static final byte[] LOG_ORDER = new byte[16];

    static {
        Arrays.fill(EMPTY_FIREWOOD, (byte) -1);
        BList numbers = new BArrayList();
        for (byte i = 0; i < 16; i++) {
            numbers.add(i);
        }
        Random random = new Random("Firewood".hashCode());
        Collections.shuffle(numbers, random);
        for (int i = 0; i < 16; i++) {
            LOG_ORDER[i] = numbers.get(i);
        }
    }

    private final BakedModel baseModel;

    public BakedModelFirewoodPile(BakedModel baseModel) {
        this.baseModel = baseModel;
    }

    public static ModelDataMap getEmptyIModelData() {
        ModelDataMap.Builder builder = new ModelDataMap.Builder();
        builder.withInitial(FIREWOOD, EMPTY_FIREWOOD);
        return builder.build();
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

    private static BakedQuad getQuadForFirewood(@Nonnull Direction whichFace,
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
        FROM.set(minX, minY, minZ);
        TO.set(maxX, maxY, maxZ);
        BlockFaceUV blockFaceUV = new BlockFaceUV(getUVs(modelFace, stateFacing, firewoodIndex),
                                                  getFaceRotation(modelFace, stateFacing, firewoodIndex));
        BlockElementFace blockPartFace = new BlockElementFace(null, -1, "", blockFaceUV);
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
        return FACE_BAKERY.bakeQuad(FROM,
                                    TO,
                                    blockPartFace,
                                    sprite,
                                    whichFace,
                                    SimpleModelState.IDENTITY,
                                    null,
                                    true,
                                    TextureManager.INTENTIONAL_MISSING_TEXTURE);
    }

    private static List<BakedQuad> getQuadsFromFirewood(Direction stateFacing, int firewoodCount, byte[] firewood, @Nullable Direction side) {
        TextureAtlas blockAtlas = ForgeModelBakery.instance().getSpriteMap().getAtlas(TextureAtlas.LOCATION_BLOCKS);
        ImmutableList.Builder<BakedQuad> builder = new ImmutableList.Builder<>();
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
                                builder.add(getQuadForFirewood(side,
                                                               blockAtlas,
                                                               4 * i,
                                                               4 * j,
                                                               0,
                                                               4 * i + 4,
                                                               4 * j + 4,
                                                               16,
                                                               firewoodIndex,
                                                               variantIndex,
                                                               Direction.NORTH,
                                                               stateFacing));
                            }
                        }
                    }
                    case SOUTH -> {
                        for (int firewoodIndex = 0; firewoodIndex < firewoodCount; firewoodIndex++) {
                            byte variantIndex = firewood[firewoodIndex];
                            if (variantIndex != -1) {
                                int i = firewoodIndex % 4;
                                int j = firewoodIndex / 4;
                                builder.add(getQuadForFirewood(side,
                                                               blockAtlas,
                                                               12 - 4 * i,
                                                               4 * j,
                                                               0,
                                                               16 - 4 * i,
                                                               4 * j + 4,
                                                               16,
                                                               firewoodIndex,
                                                               variantIndex,
                                                               Direction.NORTH,
                                                               stateFacing));
                            }
                        }
                    }
                    case WEST -> {
                        for (int firewoodIndex = 0; firewoodIndex < firewoodCount; firewoodIndex++) {
                            byte variantIndex = firewood[firewoodIndex];
                            if (variantIndex != -1) {
                                int i = firewoodIndex % 4;
                                int j = firewoodIndex / 4;
                                builder.add(getQuadForFirewood(side,
                                                               blockAtlas,
                                                               0,
                                                               4 * j,
                                                               12 - 4 * i,
                                                               16,
                                                               4 * j + 4,
                                                               16 - 4 * i,
                                                               firewoodIndex,
                                                               variantIndex,
                                                               Direction.NORTH,
                                                               stateFacing));
                            }
                        }
                    }
                    case EAST -> {
                        for (int firewoodIndex = 0; firewoodIndex < firewoodCount; firewoodIndex++) {
                            byte variantIndex = firewood[firewoodIndex];
                            if (variantIndex != -1) {
                                int i = firewoodIndex % 4;
                                int j = firewoodIndex / 4;
                                builder.add(getQuadForFirewood(side,
                                                               blockAtlas,
                                                               0,
                                                               4 * j,
                                                               4 * i,
                                                               16,
                                                               4 * j + 4,
                                                               4 * i + 4,
                                                               firewoodIndex,
                                                               variantIndex,
                                                               Direction.NORTH,
                                                               stateFacing));
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
                                builder.add(getQuadForFirewood(side,
                                                               blockAtlas,
                                                               4 * i,
                                                               4 * j,
                                                               0,
                                                               4 * i + 4,
                                                               4 * j + 4,
                                                               16,
                                                               firewoodIndex,
                                                               variantIndex,
                                                               Direction.SOUTH,
                                                               stateFacing));
                            }
                        }
                    }
                    case SOUTH -> {
                        for (int firewoodIndex = 0; firewoodIndex < firewoodCount; firewoodIndex++) {
                            byte variantIndex = firewood[firewoodIndex];
                            if (variantIndex != -1) {
                                int i = firewoodIndex % 4;
                                int j = firewoodIndex / 4;
                                builder.add(getQuadForFirewood(side,
                                                               blockAtlas,
                                                               12 - 4 * i,
                                                               4 * j,
                                                               0,
                                                               16 - 4 * i,
                                                               4 * j + 4,
                                                               16,
                                                               firewoodIndex,
                                                               variantIndex,
                                                               Direction.SOUTH,
                                                               stateFacing));
                            }
                        }
                    }
                    case WEST -> {
                        for (int firewoodIndex = 0; firewoodIndex < firewoodCount; firewoodIndex++) {
                            byte variantIndex = firewood[firewoodIndex];
                            if (variantIndex != -1) {
                                int i = firewoodIndex % 4;
                                int j = firewoodIndex / 4;
                                builder.add(getQuadForFirewood(side,
                                                               blockAtlas,
                                                               0,
                                                               4 * j,
                                                               12 - 4 * i,
                                                               16,
                                                               4 * j + 4,
                                                               16 - 4 * i,
                                                               firewoodIndex,
                                                               variantIndex,
                                                               Direction.SOUTH,
                                                               stateFacing));
                            }
                        }
                    }
                    case EAST -> {
                        for (int firewoodIndex = 0; firewoodIndex < firewoodCount; firewoodIndex++) {
                            byte variantIndex = firewood[firewoodIndex];
                            if (variantIndex != -1) {
                                int i = firewoodIndex % 4;
                                int j = firewoodIndex / 4;
                                builder.add(getQuadForFirewood(side,
                                                               blockAtlas,
                                                               0,
                                                               4 * j,
                                                               4 * i,
                                                               16,
                                                               4 * j + 4,
                                                               4 * i + 4,
                                                               firewoodIndex,
                                                               variantIndex,
                                                               Direction.SOUTH,
                                                               stateFacing));
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
                                builder.add(getQuadForFirewood(side,
                                                               blockAtlas,
                                                               12,
                                                               4 * j,
                                                               0,
                                                               16,
                                                               4 * j + 4,
                                                               16,
                                                               firewoodIndex,
                                                               variantIndex,
                                                               Direction.EAST,
                                                               stateFacing));
                            }
                        }
                    }
                    case SOUTH -> {
                        for (int firewoodIndex = 3; firewoodIndex < firewoodCount; firewoodIndex += 4) {
                            byte variantIndex = firewood[firewoodIndex];
                            if (variantIndex != -1) {
                                int j = firewoodIndex / 4;
                                builder.add(getQuadForFirewood(side,
                                                               blockAtlas,
                                                               0,
                                                               4 * j,
                                                               0,
                                                               4,
                                                               4 * j + 4,
                                                               16,
                                                               firewoodIndex,
                                                               variantIndex,
                                                               Direction.EAST,
                                                               stateFacing));
                            }
                        }
                    }
                    case WEST -> {
                        for (int firewoodIndex = 3; firewoodIndex < firewoodCount; firewoodIndex += 4) {
                            byte variantIndex = firewood[firewoodIndex];
                            if (variantIndex != -1) {
                                int j = firewoodIndex / 4;
                                builder.add(getQuadForFirewood(side,
                                                               blockAtlas,
                                                               0,
                                                               4 * j,
                                                               0,
                                                               16,
                                                               4 * j + 4,
                                                               4,
                                                               firewoodIndex,
                                                               variantIndex,
                                                               Direction.EAST,
                                                               stateFacing));
                            }
                        }
                    }
                    case EAST -> {
                        for (int firewoodIndex = 3; firewoodIndex < firewoodCount; firewoodIndex += 4) {
                            byte variantIndex = firewood[firewoodIndex];
                            if (variantIndex != -1) {
                                int j = firewoodIndex / 4;
                                builder.add(getQuadForFirewood(side,
                                                               blockAtlas,
                                                               0,
                                                               4 * j,
                                                               12,
                                                               16,
                                                               4 * j + 4,
                                                               16,
                                                               firewoodIndex,
                                                               variantIndex,
                                                               Direction.EAST,
                                                               stateFacing));
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
                                builder.add(getQuadForFirewood(side,
                                                               blockAtlas,
                                                               0,
                                                               4 * j,
                                                               0,
                                                               4,
                                                               4 * j + 4,
                                                               16,
                                                               firewoodIndex,
                                                               variantIndex,
                                                               Direction.WEST,
                                                               stateFacing));
                            }
                        }
                    }
                    case SOUTH -> {
                        for (int firewoodIndex = 0; firewoodIndex < firewoodCount; firewoodIndex += 4) {
                            byte variantIndex = firewood[firewoodIndex];
                            if (variantIndex != -1) {
                                int j = firewoodIndex / 4;
                                builder.add(getQuadForFirewood(side,
                                                               blockAtlas,
                                                               12,
                                                               4 * j,
                                                               0,
                                                               16,
                                                               4 * j + 4,
                                                               16,
                                                               firewoodIndex,
                                                               variantIndex,
                                                               Direction.WEST,
                                                               stateFacing));
                            }
                        }
                    }
                    case WEST -> {
                        for (int firewoodIndex = 0; firewoodIndex < firewoodCount; firewoodIndex += 4) {
                            byte variantIndex = firewood[firewoodIndex];
                            if (variantIndex != -1) {
                                int j = firewoodIndex / 4;
                                builder.add(getQuadForFirewood(side,
                                                               blockAtlas,
                                                               0,
                                                               4 * j,
                                                               12,
                                                               16,
                                                               4 * j + 4,
                                                               16,
                                                               firewoodIndex,
                                                               variantIndex,
                                                               Direction.WEST,
                                                               stateFacing));
                            }
                        }
                    }
                    case EAST -> {
                        for (int firewoodIndex = 0; firewoodIndex < firewoodCount; firewoodIndex += 4) {
                            byte variantIndex = firewood[firewoodIndex];
                            if (variantIndex != -1) {
                                int j = firewoodIndex / 4;
                                builder.add(getQuadForFirewood(side,
                                                               blockAtlas,
                                                               0,
                                                               4 * j,
                                                               0,
                                                               16,
                                                               4 * j + 4,
                                                               4,
                                                               firewoodIndex,
                                                               variantIndex,
                                                               Direction.WEST,
                                                               stateFacing));
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
                                builder.add(getQuadForFirewood(side,
                                                               blockAtlas,
                                                               4 * i,
                                                               12,
                                                               0,
                                                               4 * i + 4,
                                                               16,
                                                               16,
                                                               firewoodIndex,
                                                               variantIndex,
                                                               Direction.UP,
                                                               stateFacing));
                            }
                        }
                    }
                    case SOUTH -> {
                        for (int firewoodIndex = 12; firewoodIndex < firewoodCount; firewoodIndex++) {
                            byte variantIndex = firewood[firewoodIndex];
                            if (variantIndex != -1) {
                                int i = firewoodIndex % 4;
                                builder.add(getQuadForFirewood(side,
                                                               blockAtlas,
                                                               12 - 4 * i,
                                                               12,
                                                               0,
                                                               16 - 4 * i,
                                                               16,
                                                               16,
                                                               firewoodIndex,
                                                               variantIndex,
                                                               Direction.UP,
                                                               stateFacing));
                            }
                        }
                    }
                    case WEST -> {
                        for (int firewoodIndex = 12; firewoodIndex < firewoodCount; firewoodIndex++) {
                            byte variantIndex = firewood[firewoodIndex];
                            if (variantIndex != -1) {
                                int i = firewoodIndex % 4;
                                builder.add(getQuadForFirewood(side,
                                                               blockAtlas,
                                                               0,
                                                               12,
                                                               12 - 4 * i,
                                                               16,
                                                               16,
                                                               16 - 4 * i,
                                                               firewoodIndex,
                                                               variantIndex,
                                                               Direction.UP,
                                                               stateFacing));
                            }
                        }
                    }
                    case EAST -> {
                        for (int firewoodIndex = 12; firewoodIndex < firewoodCount; firewoodIndex++) {
                            byte variantIndex = firewood[firewoodIndex];
                            if (variantIndex != -1) {
                                int i = firewoodIndex % 4;
                                builder.add(getQuadForFirewood(side,
                                                               blockAtlas,
                                                               0,
                                                               12,
                                                               4 * i,
                                                               16,
                                                               16,
                                                               4 * i + 4,
                                                               firewoodIndex,
                                                               variantIndex,
                                                               Direction.UP,
                                                               stateFacing));
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
                                builder.add(getQuadForFirewood(side,
                                                               blockAtlas,
                                                               4 * i,
                                                               0,
                                                               0,
                                                               4 * i + 4,
                                                               4,
                                                               16,
                                                               firewoodIndex,
                                                               variantIndex,
                                                               Direction.DOWN,
                                                               stateFacing));
                            }
                        }
                    }
                    case SOUTH -> {
                        for (int firewoodIndex = 0; firewoodIndex < firewoodCount && firewoodIndex < 4; firewoodIndex++) {
                            byte variantIndex = firewood[firewoodIndex];
                            if (variantIndex != -1) {
                                int i = firewoodIndex % 4;
                                builder.add(getQuadForFirewood(side,
                                                               blockAtlas,
                                                               12 - 4 * i,
                                                               0,
                                                               0,
                                                               16 - 4 * i,
                                                               4,
                                                               16,
                                                               firewoodIndex,
                                                               variantIndex,
                                                               Direction.DOWN,
                                                               stateFacing));
                            }
                        }
                    }
                    case WEST -> {
                        for (int firewoodIndex = 0; firewoodIndex < firewoodCount && firewoodIndex < 4; firewoodIndex++) {
                            byte variantIndex = firewood[firewoodIndex];
                            if (variantIndex != -1) {
                                int i = firewoodIndex % 4;
                                builder.add(getQuadForFirewood(side,
                                                               blockAtlas,
                                                               0,
                                                               0,
                                                               12 - 4 * i,
                                                               16,
                                                               4,
                                                               16 - 4 * i,
                                                               firewoodIndex,
                                                               variantIndex,
                                                               Direction.DOWN,
                                                               stateFacing));
                            }
                        }
                    }
                    case EAST -> {
                        for (int firewoodIndex = 0; firewoodIndex < firewoodCount && firewoodIndex < 4; firewoodIndex++) {
                            byte variantIndex = firewood[firewoodIndex];
                            if (variantIndex != -1) {
                                int i = firewoodIndex % 4;
                                builder.add(getQuadForFirewood(side,
                                                               blockAtlas,
                                                               0,
                                                               0,
                                                               4 * i,
                                                               16,
                                                               4,
                                                               4 * i + 4,
                                                               firewoodIndex,
                                                               variantIndex,
                                                               Direction.DOWN,
                                                               stateFacing));
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
                                builder.add(getQuadForFirewood(Direction.UP,
                                                               blockAtlas,
                                                               4 * i,
                                                               4 * j,
                                                               0,
                                                               4 * i + 4,
                                                               4 * j + 4,
                                                               16,
                                                               firewoodIndex,
                                                               variantIndex,
                                                               Direction.UP,
                                                               stateFacing));
                            }
                            //Right
                            if (i != 3 && firewood[firewoodIndex + 1] == -1) {
                                builder.add(getQuadForFirewood(Direction.EAST,
                                                               blockAtlas,
                                                               4 * i,
                                                               4 * j,
                                                               0,
                                                               4 * i + 4,
                                                               4 * j + 4,
                                                               16,
                                                               firewoodIndex,
                                                               variantIndex,
                                                               Direction.EAST,
                                                               stateFacing));
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
                                builder.add(getQuadForFirewood(Direction.UP,
                                                               blockAtlas,
                                                               12 - 4 * i,
                                                               4 * j,
                                                               0,
                                                               16 - 4 * i,
                                                               4 * j + 4,
                                                               16,
                                                               firewoodIndex,
                                                               variantIndex,
                                                               Direction.UP,
                                                               stateFacing));
                            }
                            //Right
                            if (i != 3 && firewood[firewoodIndex + 1] == -1) {
                                builder.add(getQuadForFirewood(Direction.WEST,
                                                               blockAtlas,
                                                               12 - 4 * i,
                                                               4 * j,
                                                               0,
                                                               16 - 4 * i,
                                                               4 * j + 4,
                                                               16,
                                                               firewoodIndex,
                                                               variantIndex,
                                                               Direction.EAST,
                                                               stateFacing));
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
                                builder.add(getQuadForFirewood(Direction.UP,
                                                               blockAtlas,
                                                               0,
                                                               4 * j,
                                                               4 * i,
                                                               16,
                                                               4 * j + 4,
                                                               4 * i + 4,
                                                               firewoodIndex,
                                                               variantIndex,
                                                               Direction.UP,
                                                               stateFacing));
                            }
                            //Right
                            if (i != 3 && firewood[firewoodIndex + 1] == -1) {
                                builder.add(getQuadForFirewood(Direction.SOUTH,
                                                               blockAtlas,
                                                               0,
                                                               4 * j,
                                                               4 * i,
                                                               16,
                                                               4 * j + 4,
                                                               4 * i + 4,
                                                               firewoodIndex,
                                                               variantIndex,
                                                               Direction.EAST,
                                                               stateFacing));
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
                                builder.add(getQuadForFirewood(Direction.UP,
                                                               blockAtlas,
                                                               0,
                                                               4 * j,
                                                               12 - 4 * i,
                                                               16,
                                                               4 * j + 4,
                                                               16 - 4 * i,
                                                               firewoodIndex,
                                                               variantIndex,
                                                               Direction.UP,
                                                               stateFacing));
                            }
                            //Right
                            if (i != 3 && firewood[firewoodIndex + 1] == -1) {
                                builder.add(getQuadForFirewood(Direction.NORTH,
                                                               blockAtlas,
                                                               0,
                                                               4 * j,
                                                               12 - 4 * i,
                                                               16,
                                                               4 * j + 4,
                                                               16 - 4 * i,
                                                               firewoodIndex,
                                                               variantIndex,
                                                               Direction.EAST,
                                                               stateFacing));
                            }
                        }
                    }
                }
            }
        }
        return builder.build();
    }

    private static float[] getUVs(Direction modelFace, Direction stateFacing, int firewoodIndex) {
        byte realIndex = LOG_ORDER[firewoodIndex];
        int i = realIndex % 4;
        int j = realIndex / 4;
        switch (modelFace) {
            case SOUTH -> {
                switch (stateFacing) {
                    case SOUTH, WEST -> {
                        return new float[]{12 - 4 * i, 4 * j, 16 - 4 * i, 4 * j + 4};
                    }
                }
                return new float[]{4 * i, 12 - 4 * j, 4 * i + 4, 16 - 4 * j};
            }
            case NORTH -> {
                switch (stateFacing) {
                    case EAST, NORTH -> {
                        return new float[]{4 * i, 4 * j, 4 * i + 4, 4 * j + 4};
                    }
                }
                return new float[]{12 - 4 * i, 12 - 4 * j, 16 - 4 * i, 16 - 4 * j};
            }
            case UP -> {
                if (j == 3) {
                    switch (stateFacing) {
                        case SOUTH, WEST -> {
                            return new float[]{12 - 4 * i, 0, 16 - 4 * i, 16};
                        }
                    }
                    return new float[]{4 * i, 0, 4 * i + 4, 16};
                }
                return new float[]{7, 12 - 4 * i, 9, 16 - 4 * i};
            }
            case DOWN -> {
                if (j == 0) {
                    switch (stateFacing) {
                        case SOUTH, WEST -> {
                            return new float[]{4 * i, 0, 4 * i + 4, 16};
                        }
                    }
                    return new float[]{12 - 4 * i, 0, 16 - 4 * i, 16};
                }
                return new float[]{7, 12 - 4 * i, 9, 16 - 4 * i};
            }
            case EAST -> {
                if (i == 3) {
                    switch (stateFacing) {
                        case EAST, NORTH -> {
                            return new float[]{12 - 4 * j, 0, 16 - 4 * j, 16};
                        }
                    }
                    return new float[]{4 * j, 0, 4 * j + 4, 16};
                }
                return new float[]{7, 12 - 4 * j, 9, 16 - 4 * j};
            }
            case WEST -> {
                if (i == 0) {
                    switch (stateFacing) {
                        case SOUTH, WEST -> {
                            return new float[]{12 - 4 * j, 0, 16 - 4 * j, 16};
                        }
                    }
                    return new float[]{4 * j, 0, 4 * j + 4, 16};
                }
                return new float[]{7, 12 - 4 * j, 9, 16 - 4 * j};
            }
        }
        throw new IllegalStateException("Unknown direction: " + modelFace);
    }

    private List<BakedQuad> getBakedQuadsFromIModelData(@Nullable BlockState state,
                                                        @Nullable Direction side,
                                                        @Nonnull Random rand,
                                                        @Nonnull IModelData data) {
        if (!data.hasProperty(FIREWOOD)) {
            Evolution.error("IModelData did not have expected property FIREWOOD");
            return this.baseModel.getQuads(state, side, rand);
        }
        byte[] firewood = data.getData(FIREWOOD);
        return new ArrayList<>(getQuadsFromFirewood(state.getValue(EvolutionBStates.DIRECTION_HORIZONTAL),
                                                    state.getValue(EvolutionBStates.FIREWOOD_COUNT),
                                                    firewood,
                                                    side));
    }

    @Override
    @Nonnull
    public IModelData getModelData(@Nonnull BlockAndTintGetter level,
                                   @Nonnull BlockPos pos,
                                   @Nonnull BlockState state,
                                   @Nonnull IModelData tileData) {
        BlockEntity tile = level.getBlockEntity(pos);
        ModelDataMap modelDataMap = getEmptyIModelData();
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
        throw new AssertionError("IBakedModel::getQuads should never be called, only IForgeBakedModel::getQuads");
    }

    @Override
    @Nonnull
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData) {
        return this.getBakedQuadsFromIModelData(state, side, rand, extraData);
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
