package tgw.evolution.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import org.jetbrains.annotations.Nullable;

public class BlockPlaceholder extends BlockPhysics {

    public BlockPlaceholder() {
        super(Properties.of(Material.METAL).strength(2.0f, 3.0f).sound(SoundType.METAL));
    }

    @Override
    public @Nullable SoundEvent fallingSound() {
        return null;
    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return 1.0f;
    }

    @Override
    public double getMass(Level level, BlockPos pos, BlockState state) {
        return 1_000_000;
    }
}
