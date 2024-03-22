package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import org.spongepowered.asm.mixin.Mixin;
import tgw.evolution.patches.PatchBlockPredicate;

import java.util.function.BiPredicate;

@Mixin(BlockPredicate.class)
public interface MixinBlockPredicate extends BiPredicate<WorldGenLevel, BlockPos>, PatchBlockPredicate {
}
