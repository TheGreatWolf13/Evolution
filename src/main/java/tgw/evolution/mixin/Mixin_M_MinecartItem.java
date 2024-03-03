package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MinecartItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;

@Mixin(MinecartItem.class)
public abstract class Mixin_M_MinecartItem extends Item {

    @Shadow @Final AbstractMinecart.Type type;

    public Mixin_M_MinecartItem(Properties properties) {
        super(properties);
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
        BlockState state = level.getBlockState_(x, y, z);
        if (!state.is(BlockTags.RAILS)) {
            return InteractionResult.FAIL;
        }
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            RailShape railShape = state.getBlock() instanceof BaseRailBlock rail ? state.getValue(rail.getShapeProperty()) : RailShape.NORTH_SOUTH;
            double d = 0;
            if (railShape.isAscending()) {
                d = 0.5;
            }
            AbstractMinecart minecart = AbstractMinecart.createMinecart(level, x + 0.5, y + 0.062_5 + d, z + 0.5, this.type);
            if (stack.hasCustomHoverName()) {
                minecart.setCustomName(stack.getHoverName());
            }
            level.addFreshEntity(minecart);
            level.gameEvent(player, GameEvent.ENTITY_PLACE, new BlockPos(x, y, z));
        }
        stack.shrink(1);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
