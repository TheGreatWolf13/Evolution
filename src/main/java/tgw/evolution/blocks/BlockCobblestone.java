package tgw.evolution.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.init.EvolutionSounds;
import tgw.evolution.util.constants.HarvestLevel;
import tgw.evolution.util.constants.RockVariant;

public class BlockCobblestone extends BlockPhysics implements IRockVariant, ISloppable {

    private final RockVariant variant;

    public BlockCobblestone(RockVariant variant) {
        super(Properties.of(Material.STONE).strength(variant.getRockType().getHardness(), 6.0F).sound(SoundType.STONE));
        this.variant = variant;
    }

    @Override
    public boolean canSlope(BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    public boolean canSlopeFail() {
        return true;
    }

    @Override
    public SoundEvent fallingSound() {
        return EvolutionSounds.STONE_COLLAPSE.get();
    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return 1.0F;
    }

    @Override
    public int getHarvestLevel(BlockState state, @Nullable Level level, @Nullable BlockPos pos) {
        return HarvestLevel.LOW_METAL;
    }

    @Override
    public double getMass(Level level, BlockPos pos, BlockState state) {
        return this.rockVariant().getMass();
    }

    @Override
    public RockVariant rockVariant() {
        return this.variant;
    }

    @Override
    public float slopeChance() {
        return 0.5f;
    }
}
