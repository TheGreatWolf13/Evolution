package tgw.evolution.client.models.tile;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.SimpleModelTransform;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.tileentities.TEFirewoodPile;
import tgw.evolution.init.EvolutionBStates;
import tgw.evolution.init.EvolutionResources;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class BakedModelFirewoodPile implements IBakedModel {

    public static final ModelProperty<byte[]> FIREWOOD = new ModelProperty<>();
    private static final byte[] EMPTY_FIREWOOD = new byte[16];
    private static final Vector3f FROM = new Vector3f();
    private static final Vector3f TO = new Vector3f();
    private static final FaceBakery FACE_BAKERY = new FaceBakery();
    private static final byte[] LOG_ORDER = new byte[16];

    static {
        Arrays.fill(EMPTY_FIREWOOD, (byte) -1);
        List<Byte> numbers = new ArrayList<>();
        for (byte i = 0; i < 16; i++) {
            numbers.add(i);
        }
        Random random = new Random("Firewood".hashCode());
        Collections.shuffle(numbers, random);
        for (int i = 0; i < 16; i++) {
            LOG_ORDER[i] = numbers.get(i);
        }
    }

    private final IBakedModel baseModel;

    public BakedModelFirewoodPile(IBakedModel baseModel) {
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
            case NORTH: {
                switch (stateFacing) {
                    case EAST:
                    case NORTH: {
                        return 180;
                    }
                }
                return 0;
            }
            case SOUTH: {
                switch (stateFacing) {
                    case SOUTH:
                    case WEST: {
                        return 180;
                    }
                }
                return 0;
            }
            case UP: {
                if (j == 3) {
                    switch (stateFacing) {
                        case EAST:
                        case WEST: {
                            return 90;
                        }
                    }
                    return 0;
                }
                switch (stateFacing) {
                    case NORTH: {
                        return 90;
                    }
                    case EAST: {
                        return 180;
                    }
                    case SOUTH: {
                        return 270;
                    }
                }
                return 0;
            }
            case DOWN: {
                if (j == 0) {
                    switch (stateFacing) {
                        case EAST:
                        case WEST: {
                            return 90;
                        }
                    }
                    return 180;
                }
                switch (stateFacing) {
                    case NORTH: {
                        return 90;
                    }
                    case WEST: {
                        return 180;
                    }
                    case SOUTH: {
                        return 270;
                    }
                }
                return 0;
            }
            case EAST: {
                if (i == 3) {
                    switch (stateFacing) {
                        case EAST:
                        case NORTH: {
                            return 90;
                        }
                    }
                    return 270;
                }
                return 0;
            }
            case WEST: {
                if (i == 0) {
                    switch (stateFacing) {
                        case SOUTH:
                        case WEST: {
                            return 90;
                        }
                    }
                    return 270;
                }
                return 0;
            }
        }
        throw new IllegalStateException("Unknown direction: " + modelFace);
    }

    private static BakedQuad getQuadForFirewood(@Nonnull Direction whichFace,
                                                AtlasTexture blockAtlas,
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
        BlockPartFace blockPartFace = new BlockPartFace(null, -1, "", blockFaceUV);
        TextureAtlasSprite sprite;
        byte realIndex = LOG_ORDER[firewoodIndex];
        switch (modelFace) {
            case NORTH:
            case SOUTH: {
                sprite = blockAtlas.getSprite(EvolutionResources.BLOCK_LOG_TOP[variantId]);
                break;
            }
            case DOWN: {
                if (realIndex < 4) {
                    sprite = blockAtlas.getSprite(EvolutionResources.BLOCK_LOG_SIDE[variantId]);
                }
                else {
                    sprite = blockAtlas.getSprite(EvolutionResources.BLOCK_LOG_TOP[variantId]);
                }
                break;
            }
            case UP: {
                if (realIndex >= 12) {
                    sprite = blockAtlas.getSprite(EvolutionResources.BLOCK_LOG_SIDE[variantId]);
                }
                else {
                    sprite = blockAtlas.getSprite(EvolutionResources.BLOCK_LOG_TOP[variantId]);
                }
                break;
            }
            case EAST: {
                if (realIndex % 4 == 3) {
                    sprite = blockAtlas.getSprite(EvolutionResources.BLOCK_LOG_SIDE[variantId]);
                }
                else {
                    sprite = blockAtlas.getSprite(EvolutionResources.BLOCK_LOG_TOP[variantId]);
                }
                break;
            }
            case WEST: {
                if (realIndex % 4 == 0) {
                    sprite = blockAtlas.getSprite(EvolutionResources.BLOCK_LOG_SIDE[variantId]);
                }
                else {
                    sprite = blockAtlas.getSprite(EvolutionResources.BLOCK_LOG_TOP[variantId]);
                }
                break;
            }
            default: {
                throw new IllegalStateException("Unknown direction: " + modelFace);
            }
        }
        return FACE_BAKERY.bakeQuad(FROM,
                                    TO,
                                    blockPartFace,
                                    sprite,
                                    whichFace,
                                    SimpleModelTransform.IDENTITY,
                                    null,
                                    true,
                                    TextureManager.INTENTIONAL_MISSING_TEXTURE);
    }

    private static List<BakedQuad> getQuadsFromFirewood(Direction stateFacing, int firewoodCount, byte[] firewood, @Nullable Direction side) {
        AtlasTexture blockAtlas = ModelLoader.instance().getSpriteMap().getAtlas(AtlasTexture.LOCATION_BLOCKS);
        ImmutableList.Builder<BakedQuad> builder = new ImmutableList.Builder<>();
        if (side != null) {
            if (stateFacing == side) {
                //Back face -> North
                switch (stateFacing) {
                    case NORTH: {
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
                        break;
                    }
                    case SOUTH: {
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
                        break;
                    }
                    case WEST: {
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
                        break;
                    }
                    case EAST: {
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
                        break;
                    }
                }
            }
            else if (stateFacing.getOpposite() == side) {
                //Front face -> South
                switch (stateFacing) {
                    case NORTH: {
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
                        break;
                    }
                    case SOUTH: {
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
                        break;
                    }
                    case WEST: {
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
                        break;
                    }
                    case EAST: {
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
                        break;
                    }
                }
            }
            else if (stateFacing.getClockWise() == side) {
                //Right face -> East
                switch (stateFacing) {
                    case NORTH: {
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
                        break;
                    }
                    case SOUTH: {
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
                        break;
                    }
                    case WEST: {
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
                        break;
                    }
                    case EAST: {
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
                        break;
                    }
                }
            }
            else if (stateFacing.getCounterClockWise() == side) {
                //Left face -> West
                switch (stateFacing) {
                    case NORTH: {
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
                        break;
                    }
                    case SOUTH: {
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
                        break;
                    }
                    case WEST: {
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
                        break;
                    }
                    case EAST: {
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
                        break;
                    }
                }
            }
            else if (side == Direction.UP) {
                //Top face -> Up
                switch (stateFacing) {
                    case NORTH: {
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
                        break;
                    }
                    case SOUTH: {
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
                        break;
                    }
                    case WEST: {
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
                        break;
                    }
                    case EAST: {
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
                        break;
                    }
                }
            }
            else {
                //Bottom face -> Down
                switch (stateFacing) {
                    case NORTH: {
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
                        break;
                    }
                    case SOUTH: {
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
                        break;
                    }
                    case WEST: {
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
                        break;
                    }
                    case EAST: {
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
                        break;
                    }
                }
            }
        }
        else {
            switch (stateFacing) {
                case NORTH: {
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
                    break;
                }
                case SOUTH: {
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
                    break;
                }
                case EAST: {
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
                    break;
                }
                case WEST: {
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
                    break;
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
            case SOUTH: {
                switch (stateFacing) {
                    case SOUTH:
                    case WEST: {
                        return new float[]{12 - 4 * i, 4 * j, 16 - 4 * i, 4 * j + 4};
                    }
                }
                return new float[]{4 * i, 12 - 4 * j, 4 * i + 4, 16 - 4 * j};
            }
            case NORTH: {
                switch (stateFacing) {
                    case EAST:
                    case NORTH: {
                        return new float[]{4 * i, 4 * j, 4 * i + 4, 4 * j + 4};
                    }
                }
                return new float[]{12 - 4 * i, 12 - 4 * j, 16 - 4 * i, 16 - 4 * j};
            }
            case UP: {
                if (j == 3) {
                    switch (stateFacing) {
                        case SOUTH:
                        case WEST: {
                            return new float[]{12 - 4 * i, 0, 16 - 4 * i, 16};
                        }
                    }
                    return new float[]{4 * i, 0, 4 * i + 4, 16};
                }
                return new float[]{7, 12 - 4 * i, 9, 16 - 4 * i};
            }
            case DOWN: {
                if (j == 0) {
                    switch (stateFacing) {
                        case SOUTH:
                        case WEST: {
                            return new float[]{4 * i, 0, 4 * i + 4, 16};
                        }
                    }
                    return new float[]{12 - 4 * i, 0, 16 - 4 * i, 16};
                }
                return new float[]{7, 12 - 4 * i, 9, 16 - 4 * i};
            }
            case EAST: {
                if (i == 3) {
                    switch (stateFacing) {
                        case EAST:
                        case NORTH: {
                            return new float[]{12 - 4 * j, 0, 16 - 4 * j, 16};
                        }
                    }
                    return new float[]{4 * j, 0, 4 * j + 4, 16};
                }
                return new float[]{7, 12 - 4 * j, 9, 16 - 4 * j};
            }
            case WEST: {
                if (i == 0) {
                    switch (stateFacing) {
                        case SOUTH:
                        case WEST: {
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
            Evolution.LOGGER.error("IModelData did not have expected property FIREWOOD");
            return this.baseModel.getQuads(state, side, rand);
        }
        byte[] firewood = data.getData(FIREWOOD);
        return new LinkedList<>(getQuadsFromFirewood(state.getValue(EvolutionBStates.DIRECTION_HORIZONTAL),
                                                     state.getValue(EvolutionBStates.FIREWOOD_COUNT),
                                                     firewood,
                                                     side));
    }

    @Override
    @Nonnull
    public IModelData getModelData(@Nonnull IBlockDisplayReader world,
                                   @Nonnull BlockPos pos,
                                   @Nonnull BlockState state,
                                   @Nonnull IModelData tileData) {
        TileEntity tile = world.getBlockEntity(pos);
        ModelDataMap modelDataMap = getEmptyIModelData();
        if (tile instanceof TEFirewoodPile) {
            modelDataMap.setData(FIREWOOD, ((TEFirewoodPile) tile).getFirewood());
        }
        else {
            modelDataMap.setData(FIREWOOD, EMPTY_FIREWOOD);
        }
        return modelDataMap;
    }

    @Override
    public ItemOverrideList getOverrides() {
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
