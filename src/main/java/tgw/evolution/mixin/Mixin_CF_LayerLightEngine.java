package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.*;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.Evolution;
import tgw.evolution.hooks.asm.DeleteField;
import tgw.evolution.hooks.asm.DummyConstructor;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.hooks.asm.RestoreFinal;

@Mixin(LayerLightEngine.class)
public abstract class Mixin_CF_LayerLightEngine<M extends DataLayerStorageMap<M>, S extends LayerLightSectionStorage<M>>
        extends DynamicGraphMinFixedPoint implements LayerLightEventListener {

    @Mutable @Shadow @Final @RestoreFinal public S storage;
    @Mutable @Shadow @Final @RestoreFinal protected LightChunkGetter chunkSource;
    @Mutable @Shadow @Final @RestoreFinal protected LightLayer layer;
    @Shadow @Final @DeleteField protected BlockPos.MutableBlockPos pos;
    @Mutable @Shadow @Final @RestoreFinal private BlockGetter[] lastChunk;
    @Mutable @Shadow @Final @RestoreFinal private long[] lastChunkPos;

    @DummyConstructor
    public Mixin_CF_LayerLightEngine(int i, int j, int k) {
        super(i, j, k);
    }

    @ModifyConstructor
    public Mixin_CF_LayerLightEngine(LightChunkGetter lightChunkGetter, LightLayer lightLayer, S layerLightSectionStorage) {
        super(16, 256, 8_192);
        this.lastChunkPos = new long[2];
        this.lastChunk = new BlockGetter[2];
        this.chunkSource = lightChunkGetter;
        this.layer = lightLayer;
        this.storage = layerLightSectionStorage;
        this.clearCache();
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    public void checkBlock(BlockPos pos) {
        Evolution.deprecatedMethod();
        this.checkBlock_(pos.asLong());
    }

    @Override
    public void checkBlock_(long pos) {
        this.checkNode(pos);
        int x = BlockPos.getX(pos);
        int y = BlockPos.getY(pos);
        int z = BlockPos.getZ(pos);
        this.checkNode(BlockPos.asLong(x - 1, y, z));
        this.checkNode(BlockPos.asLong(x + 1, y, z));
        this.checkNode(BlockPos.asLong(x, y - 1, z));
        this.checkNode(BlockPos.asLong(x, y + 1, z));
        this.checkNode(BlockPos.asLong(x, y, z - 1));
        this.checkNode(BlockPos.asLong(x, y, z + 1));
    }

    @Shadow
    protected abstract void clearCache();

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    public void enableLightSources(ChunkPos chunkPos, boolean bl) {
        Evolution.deprecatedMethod();
        this.enableLightSources_(chunkPos.x, chunkPos.z, bl);
    }

    @Override
    public void enableLightSources_(int secX, int secZ, boolean bl) {
        long l = SectionPos.getZeroNode(SectionPos.asLong(secX, 0, secZ));
        this.storage.enableLightSources(l, bl);
    }

    @Shadow
    protected abstract @Nullable BlockGetter getChunk(int i, int j);

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    public int getLightValue(BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.getLightValue_(pos.asLong());
    }

    @Override
    public int getLightValue_(long pos) {
        return this.storage.getLightValue(pos);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public VoxelShape getShape(BlockState state, long pos, Direction direction) {
        return state.canOcclude() ?
               state.getFaceOcclusionShape_(this.chunkSource.getLevel(), BlockPos.getX(pos), BlockPos.getY(pos), BlockPos.getZ(pos), direction) :
               Shapes.empty();
    }

    /**
     * @author TheGreatWolf
     * @reason Use non-BlockPos version
     */
    @Overwrite
    public BlockState getStateAndOpacity(long pos, @Nullable MutableInt opacity) {
        if (pos == Long.MAX_VALUE) {
            if (opacity != null) {
                opacity.setValue(0);
            }
            return Blocks.AIR.defaultBlockState();
        }
        int secX = SectionPos.blockToSectionCoord(BlockPos.getX(pos));
        int secZ = SectionPos.blockToSectionCoord(BlockPos.getZ(pos));
        BlockGetter chunk = this.getChunk(secX, secZ);
        if (chunk == null) {
            if (opacity != null) {
                opacity.setValue(16);
            }
            return Blocks.BEDROCK.defaultBlockState();
        }
        int x = BlockPos.getX(pos);
        int y = BlockPos.getY(pos);
        int z = BlockPos.getZ(pos);
        BlockState blockState = chunk.getBlockState_(x, y, z);
        boolean blocksLight = blockState.canOcclude() && blockState.useShapeForLightOcclusion();
        if (opacity != null) {
            opacity.setValue(blockState.getLightBlock_(this.chunkSource.getLevel(), x, y, z));
        }
        return blocksLight ? blockState : Blocks.AIR.defaultBlockState();
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    public void onBlockEmissionIncrease(BlockPos pos, int lightEmission) {
        Evolution.deprecatedMethod();
        this.onBlockEmissionIncrease_(pos.asLong(), lightEmission);
    }

    @Override
    public void onBlockEmissionIncrease_(long pos, int lightEmission) {
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    public void updateSectionStatus(SectionPos secPos, boolean hasOnlyAir) {
        Evolution.deprecatedMethod();
        this.updateSectionStatus_sec(secPos.x(), secPos.y(), secPos.z(), hasOnlyAir);
    }

    @Override
    public void updateSectionStatus_sec(int secX, int secY, int secZ, boolean hasOnlyAir) {
        this.storage.updateSectionStatus(SectionPos.asLong(secX, secY, secZ), hasOnlyAir);
    }
}
