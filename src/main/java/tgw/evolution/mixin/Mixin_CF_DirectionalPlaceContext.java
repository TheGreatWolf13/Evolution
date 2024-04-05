package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.hooks.asm.RestoreFinal;
import tgw.evolution.patches.PatchBlockHitResult;

@Mixin(DirectionalPlaceContext.class)
public abstract class Mixin_CF_DirectionalPlaceContext extends BlockPlaceContext {

    @Mutable @Shadow @Final @RestoreFinal private Direction direction;

    @ModifyConstructor
    public Mixin_CF_DirectionalPlaceContext(Level level, BlockPos pos, Direction direction, ItemStack stack, Direction direction2) {
        super(level, null, InteractionHand.MAIN_HAND, stack, PatchBlockHitResult.create(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, direction2, pos.getX(), pos.getY(), pos.getZ(), false));
        this.direction = direction;
    }
}
