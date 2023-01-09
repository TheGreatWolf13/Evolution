package tgw.evolution.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import tgw.evolution.init.EvolutionSounds;
import tgw.evolution.util.constants.RockVariant;

public class BlockDirt extends BlockPhysics implements IRockVariant, ISloppable {

    private final RockVariant variant;

    public BlockDirt(RockVariant variant) {
        super(Properties.of(Material.DIRT).strength(2.0F, 0.5F).sound(SoundType.GRAVEL));
        this.variant = variant;
    }

    @Override
    public boolean canSlope(BlockGetter level, BlockPos pos) {
        Block blockUp = BlockUtils.getBlockState(level, pos.getX(), pos.getY() + 1, pos.getZ()).getBlock();
        if (blockUp == this.variant.getGrass()) {
            return false;
        }
        return blockUp != this || BlockUtils.getBlockState(level, pos.getX(), pos.getY() + 2, pos.getZ()).getBlock() != this.variant.getGrass();
    }

    @Override
    public boolean canSlopeFail() {
        return false;
    }

    @Override
    public SoundEvent fallingSound() {
        return EvolutionSounds.SOIL_COLLAPSE.get();
    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return 0.55f;
    }

    @Override
    public double getMass(Level level, BlockPos pos, BlockState state) {
        return this.rockVariant().getMass() / 4;
    }

    @Override
    public RockVariant rockVariant() {
        return this.variant;
    }

    @Override
    public float slopeChance() {
        return 1;
    }
}
