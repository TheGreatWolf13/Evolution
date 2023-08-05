package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.lighting.LightEventListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.Evolution;
import tgw.evolution.patches.PatchLightEventListener;

@Mixin(LightEventListener.class)
public interface MixinLightEventListener extends PatchLightEventListener {

    @Overwrite
    default void updateSectionStatus(BlockPos pos, boolean hasOnlyAir) {
        Evolution.deprecatedMethod();
        this.updateSectionStatus_block(pos.getX(), pos.getY(), pos.getZ(), hasOnlyAir);
    }
}
