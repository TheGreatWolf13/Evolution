package tgw.evolution.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import tgw.evolution.init.EvolutionSounds;
import tgw.evolution.util.RockVariant;

public class BlockDirt extends BlockGravity implements IRockVariant {

    private final RockVariant variant;

    public BlockDirt(RockVariant variant) {
        super(Properties.of(Material.DIRT).strength(2.0F, 0.5F).sound(SoundType.GRAVEL), variant.getMass() / 4);
        this.variant = variant;
    }

    @Override
    public boolean canSlope() {
        return true;
    }

    @Override
    public SoundEvent fallSound() {
        return EvolutionSounds.SOIL_COLLAPSE.get();
    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return 0.55f;
    }

    @Override
    public RockVariant getVariant() {
        return this.variant;
    }

    @Override
    public boolean preventSlope(World world, BlockPos pos) {
        BlockPos up = pos.above();
        if (world.getBlockState(up).getBlock() == this.variant.getGrass()) {
            return true;
        }
        return world.getBlockState(up).getBlock() == this && world.getBlockState(pos.above(2)).getBlock() == this.variant.getGrass();
    }
}
