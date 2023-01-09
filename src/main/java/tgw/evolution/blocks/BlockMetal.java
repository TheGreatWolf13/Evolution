package tgw.evolution.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.blocks.tileentities.TEMetal;
import tgw.evolution.util.constants.MetalVariant;
import tgw.evolution.util.constants.Oxidation;

import java.util.Random;

public class BlockMetal extends BlockPhysics implements EntityBlock {

    private final MetalVariant metal;
    private final Oxidation oxidation;

    public BlockMetal(MetalVariant variant, Oxidation oxidation) {
        super(Properties.of(Material.METAL)
                        .sound(variant == MetalVariant.COPPER ? SoundType.COPPER : SoundType.METAL)
                        .strength(variant.getHardness(oxidation), variant.getResistance(oxidation)));
        this.metal = variant;
        this.oxidation = oxidation;
    }

//    @Override
//    public SoundEvent fallSound() {
//        return SoundEvents.ANVIL_LAND;
//    }

    @Override
    public @Nullable SoundEvent fallingSound() {
        //TODO implementation
        return null;
    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return this.metal.getFrictionCoefficient();
    }

    @Override
    public int getHarvestLevel(BlockState state, @Nullable Level level, @Nullable BlockPos pos) {
        return this.metal.getHarvestLevel();
    }

    @Override
    public double getMass(Level level, BlockPos pos, BlockState state) {
        //TODO implementation
        return 0;
    }

    public MetalVariant getMetal() {
        return this.metal;
    }

    public Oxidation getOxidation() {
        return this.oxidation;
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return this.metal.doesOxidize() && this.oxidation != Oxidation.OXIDIZED;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (this.metal.doesOxidize()) {
            return new TEMetal(pos, state);
        }
        return null;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!level.isClientSide && this.metal.doesOxidize() && this.oxidation != Oxidation.OXIDIZED) {
            TEMetal tile = (TEMetal) level.getBlockEntity(pos);
            assert tile != null;
            tile.oxidationTick(this.metal, this.oxidation);
        }
        super.onPlace(state, level, pos, oldState, isMoving);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, Random rand) {
        if (this.metal.doesOxidize() && this.oxidation != Oxidation.OXIDIZED) {
            TEMetal tile = (TEMetal) level.getBlockEntity(pos);
            assert tile != null;
            tile.oxidationTick(this.metal, this.oxidation);
        }
    }

    @Override
    public boolean triggerEvent(BlockState state, Level level, BlockPos pos, int paramA, int paramB) {
        if (!level.isClientSide && this.metal.doesOxidize() && this.oxidation != Oxidation.OXIDIZED) {
            TEMetal currentTile = (TEMetal) level.getBlockEntity(pos);
            assert currentTile != null;
            if (currentTile.shouldOxidize(this.metal, this.oxidation)) {
                Oxidation nextOxidation = this.oxidation.getNextStage();
                level.setBlockAndUpdate(pos, this.metal.getBlock(nextOxidation).defaultBlockState());
                if (nextOxidation != Oxidation.OXIDIZED) {
                    TEMetal newTile = (TEMetal) level.getBlockEntity(pos);
                    assert newTile != null;
                    newTile.updateFromOld(currentTile);
                    newTile.oxidationTick(this.metal, nextOxidation);
                    level.blockEvent(pos, level.getBlockState(pos).getBlock(), 0, 0);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState fromState, LevelAccessor level, BlockPos pos, BlockPos fromPos) {
        if (!level.isClientSide() && this.metal.doesOxidize() && this.oxidation != Oxidation.OXIDIZED) {
            TEMetal tile = (TEMetal) level.getBlockEntity(pos);
            if (tile != null) {
                tile.oxidationTick(this.metal, this.oxidation);
            }
        }
        return super.updateShape(state, direction, fromState, level, pos, fromPos);
    }
}
