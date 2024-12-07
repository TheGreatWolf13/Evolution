package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.BlockLightEngine;
import net.minecraft.world.level.lighting.BlockLightSectionStorage;
import net.minecraft.world.level.lighting.LayerLightEngine;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.mutable.MutableInt;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.Evolution;
import tgw.evolution.EvolutionClient;
import tgw.evolution.client.renderer.ambient.DynamicLights;
import tgw.evolution.hooks.asm.DeleteField;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.patches.PatchBlockLightEngine;

@Mixin(BlockLightEngine.class)
public abstract class Mixin_CF_BlockLightEngine extends LayerLightEngine<BlockLightSectionStorage.BlockDataLayerStorageMap, BlockLightSectionStorage> implements PatchBlockLightEngine {

    @Unique private final boolean isClientSide;
    @Shadow @Final @DeleteField private BlockPos.MutableBlockPos pos;

    @ModifyConstructor
    public Mixin_CF_BlockLightEngine(LightChunkGetter lightChunkGetter) {
        super(lightChunkGetter, LightLayer.BLOCK, new BlockLightSectionStorage(lightChunkGetter));
        this.isClientSide = lightChunkGetter.getLevel() instanceof Level level && level.isClientSide;
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    public int computeLevelFromNeighbor(long otherPos_, long pos_, int someLight_) {
        if (pos_ == Long.MAX_VALUE) {
            return 15;
        }
        if (otherPos_ == Long.MAX_VALUE) {
            return someLight_ + 15 - this.getLightEmission(pos_);
        }
        if (someLight_ >= 15) {
            return someLight_;
        }
        int j = Integer.signum(BlockPos.getX(pos_) - BlockPos.getX(otherPos_));
        int k = Integer.signum(BlockPos.getY(pos_) - BlockPos.getY(otherPos_));
        int n = Integer.signum(BlockPos.getZ(pos_) - BlockPos.getZ(otherPos_));
        Direction direction = Direction.fromNormal(j, k, n);
        if (direction == null) {
            return 15;
        }
        MutableInt mutableInt = new MutableInt();
        BlockState blockState = this.getStateAndOpacity(pos_, mutableInt);
        if (mutableInt.getValue() >= 15) {
            return 15;
        }
        BlockState blockState2 = this.getStateAndOpacity(otherPos_, null);
        VoxelShape voxelShape = this.getShape(blockState2, otherPos_, direction);
        VoxelShape voxelShape2 = this.getShape(blockState, pos_, direction.getOpposite());
        return Shapes.faceShapeOccludes(voxelShape, voxelShape2) ? 15 : someLight_ + Math.max(1, mutableInt.getValue());
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    private int getLightEmission(long pos) {
        int dl = 0;
        if (this.isClientSide) {
            dl = EvolutionClient.getDynamicLights().getLight(pos);
            if (dl == 0b1_1111_1_1111_1_1111) {
                return 0b1_1111_1_1111_1_1111;
            }
        }
        int x = BlockPos.getX(pos);
        int y = BlockPos.getY(pos);
        int z = BlockPos.getZ(pos);
        BlockGetter blockGetter = this.chunkSource.getChunkForLighting(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z));
        int bl = blockGetter != null ? blockGetter.getLightEmission_(x, y, z) : 0;
        if (bl == 0) {
            return dl;
        }
        if (dl == 0) {
            return bl;
        }
        return DynamicLights.combine(bl, dl);
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
        this.storage.runAllUpdates();
        this.checkEdge(Long.MAX_VALUE, pos, 15 - lightEmission, true);
    }

    @Override
    public void setColor(boolean red, boolean green, boolean blue) {
//        assert red && !green && !blue || !red && green && !blue || !red && !green && blue;
//        if (red) {
//            this.flags |= 2;
//        }
//        else if (green) {
//            this.flags |= 4;
//        }
//        else {
//            this.flags |= 8;
//        }
    }
}
