package tgw.evolution.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.util.constants.HarvestLevel;
import tgw.evolution.util.constants.RockVariant;

public class BlockStoneBricks extends BlockPhysics implements IRockVariant {

    private final RockVariant variant;

    public BlockStoneBricks(RockVariant variant) {
        super(Properties.of(Material.STONE).strength(variant.getRockType().getHardness(), 8.0F).sound(SoundType.STONE));
        this.variant = variant;
    }

//    @Override
//    public int beamSize() {
//        return this.variant.getRockType().getRangeStone() + 4;
//    }

    @Override
    public @Nullable SoundEvent fallingSound() {
        //TODO implementation
        return null;
    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return 1.0f;
    }

    @Override
    public int getHarvestLevel(BlockState state, @Nullable Level level, @Nullable BlockPos pos) {
        return HarvestLevel.LOW_METAL;
    }

    @Override
    public double getMass(Level level, BlockPos pos, BlockState state) {
        //TODO implementation
        return 0;
    }

//    @Override
//    public int getShearStrength() {
//        return (int) (this.variant.getShearStrength() * 1.2);
//    }

    @Override
    public RockVariant rockVariant() {
        return this.variant;
    }
}
