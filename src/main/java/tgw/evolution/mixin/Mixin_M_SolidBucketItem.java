package tgw.evolution.mixin;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DispensibleContainerItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SolidBucketItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.hooks.asm.DeleteMethod;

@Mixin(SolidBucketItem.class)
public abstract class Mixin_M_SolidBucketItem extends BlockItem implements DispensibleContainerItem {

    public Mixin_M_SolidBucketItem(Block block, Properties properties) {
        super(block, properties);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public InteractionResult useOn(UseOnContext useOnContext) {
        throw new AbstractMethodError();
    }

    @Override
    public InteractionResult useOn_(Level level, int x, int y, int z, Player player, InteractionHand hand, BlockHitResult hitResult) {
        InteractionResult result = super.useOn_(level, x, y, z, player, hand, hitResult);
        if (result.consumesAction() && !player.isCreative()) {
            player.setItemInHand(hand, Items.BUCKET.getDefaultInstance());
        }
        return result;
    }
}
