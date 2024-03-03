package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(UseOnContext.class)
public abstract class MixinUseOnContext {

    @Shadow @Final private BlockHitResult hitResult;

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public BlockPos getClickedPos() {
        return new BlockPos(this.hitResult.posX(), this.hitResult.posY(), this.hitResult.posZ());
    }
}
