package tgw.evolution.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import tgw.evolution.blocks.tileentities.TEMetal;
import tgw.evolution.init.EvolutionSounds;
import tgw.evolution.util.MetalVariant;
import tgw.evolution.util.Oxidation;

import javax.annotation.Nullable;
import java.util.Random;

public class BlockMetal extends BlockGravity {

    private final MetalVariant metal;
    private final Oxidation oxidation;

    public BlockMetal(MetalVariant variant, Oxidation oxidation) {
        super(Properties.of(Material.METAL)
                        .harvestLevel(variant.getHarvestLevel())
                        .sound(variant == MetalVariant.COPPER ? EvolutionSounds.COPPER : SoundType.METAL)
                        .strength(variant.getHardness(oxidation), variant.getResistance(oxidation)), variant.getDensity());
        this.metal = variant;
        this.oxidation = oxidation;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        if (this.metal.doesOxidize()) {
            return new TEMetal();
        }
        return super.createTileEntity(state, world);
    }

    @Override
    public SoundEvent fallSound() {
        return SoundEvents.ANVIL_LAND;
    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return this.metal.getFrictionCoefficient();
    }

    public MetalVariant getMetal() {
        return this.metal;
    }

    public Oxidation getOxidation() {
        return this.oxidation;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return this.metal.doesOxidize() && this.oxidation != Oxidation.OXIDIZED;
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return this.metal.doesOxidize() && this.oxidation != Oxidation.OXIDIZED;
    }

    @Override
    public void onPlace(BlockState state, World world, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!world.isClientSide && this.metal.doesOxidize() && this.oxidation != Oxidation.OXIDIZED) {
            TEMetal tile = (TEMetal) world.getBlockEntity(pos);
            tile.oxidationTick(this.metal, this.oxidation);
        }
        super.onPlace(state, world, pos, oldState, isMoving);
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random rand) {
        if (this.metal.doesOxidize() && this.oxidation != Oxidation.OXIDIZED) {
            TEMetal tile = (TEMetal) world.getBlockEntity(pos);
            tile.oxidationTick(this.metal, this.oxidation);
        }
    }

    @Override
    public boolean triggerEvent(BlockState state, World world, BlockPos pos, int paramA, int paramB) {
        if (!world.isClientSide && this.metal.doesOxidize() && this.oxidation != Oxidation.OXIDIZED) {
            TEMetal currentTile = (TEMetal) world.getBlockEntity(pos);
            if (currentTile.shouldOxidize(this.metal, this.oxidation)) {
                Oxidation nextOxidation = this.oxidation.getNextStage();
                world.setBlockAndUpdate(pos, this.metal.getBlock(nextOxidation).defaultBlockState());
                if (nextOxidation != Oxidation.OXIDIZED) {
                    TEMetal newTile = (TEMetal) world.getBlockEntity(pos);
                    newTile.updateFromOld(currentTile);
                    newTile.oxidationTick(this.metal, nextOxidation);
                    world.blockEvent(pos, world.getBlockState(pos).getBlock(), 0, 0);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction dir, BlockState facingState, IWorld world, BlockPos pos, BlockPos facingPos) {
        if (!world.isClientSide() && this.metal.doesOxidize() && this.oxidation != Oxidation.OXIDIZED) {
            TEMetal tile = (TEMetal) world.getBlockEntity(pos);
            if (tile != null) {
                tile.oxidationTick(this.metal, this.oxidation);
            }
        }
        return super.updateShape(state, dir, facingState, world, pos, facingPos);
    }
}
