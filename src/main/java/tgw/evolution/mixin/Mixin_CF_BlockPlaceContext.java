package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.hooks.asm.RestoreFinal;

@Mixin(BlockPlaceContext.class)
public abstract class Mixin_CF_BlockPlaceContext extends UseOnContext {

    @Shadow protected boolean replaceClicked;
    @Mutable @Shadow @Final @RestoreFinal private BlockPos relativePos;

    @ModifyConstructor
    protected Mixin_CF_BlockPlaceContext(Level level, @Nullable Player player, InteractionHand hand, ItemStack stack, BlockHitResult hitResult) {
        super(level, player, hand, stack, hitResult);
        int x = hitResult.posX();
        int y = hitResult.posY();
        int z = hitResult.posZ();
        Direction dir = hitResult.getDirection();
        this.relativePos = new BlockPos(x + dir.getStepX(), y + dir.getStepY(), z + dir.getStepZ());
        this.replaceClicked = true;
        this.replaceClicked = level.getBlockState_(x, y, z).canBeReplaced((BlockPlaceContext) (Object) this);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public boolean canPlace() {
        return this.replaceClicked || this.getLevel().getBlockState_(this.getClickedPos()).canBeReplaced((BlockPlaceContext) (Object) this);
    }
}
