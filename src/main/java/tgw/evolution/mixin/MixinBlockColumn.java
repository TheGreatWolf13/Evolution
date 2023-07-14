package tgw.evolution.mixin;

import net.minecraft.world.level.chunk.BlockColumn;
import org.spongepowered.asm.mixin.Mixin;
import tgw.evolution.patches.PatchBlockColumn;

@Mixin(BlockColumn.class)
public interface MixinBlockColumn extends PatchBlockColumn {

}
