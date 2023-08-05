package tgw.evolution.mixin;

import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.hooks.asm.DeleteMethod;

@Mixin(FireworkRocketItem.class)
public abstract class Mixin_M_FireworkRocketItem extends Item {

    public Mixin_M_FireworkRocketItem(Properties properties) {
        super(properties);
    }

    @Override
    @Overwrite
    @DeleteMethod
    public InteractionResult useOn(UseOnContext useOnContext) {
        throw new AbstractMethodError();
    }

    @Override
    public InteractionResult useOn_(Level level, int x, int y, int z, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            ItemStack stack = player.getItemInHand(hand);
            Direction direction = hitResult.getDirection();
            FireworkRocketEntity rocketEntity = new FireworkRocketEntity(level, player,
                                                                         hitResult.x() + direction.getStepX() * 0.15,
                                                                         hitResult.y() + direction.getStepY() * 0.15,
                                                                         hitResult.z() + direction.getStepZ() * 0.15, stack);
            level.addFreshEntity(rocketEntity);
            stack.shrink(1);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
