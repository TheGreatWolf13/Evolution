package tgw.evolution.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import tgw.evolution.init.EvolutionSounds;
import tgw.evolution.util.constants.RockVariant;

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
    public boolean preventSlope(Level level, BlockPos pos) {
        BlockPos up = pos.above();
        if (level.getBlockState(up).getBlock() == this.variant.getGrass()) {
            return true;
        }
        return level.getBlockState(up).getBlock() == this && level.getBlockState(pos.above(2)).getBlock() == this.variant.getGrass();
    }
}
