package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.GlowItemFrame;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HangingEntityItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;

@Mixin(HangingEntityItem.class)
public abstract class Mixin_M_HangingEntityItem extends Item {

    @Shadow @Final private EntityType<? extends HangingEntity> type;

    public Mixin_M_HangingEntityItem(Properties properties) {
        super(properties);
    }

    @Shadow
    protected abstract boolean mayPlace(Player player, Direction direction, ItemStack itemStack, BlockPos blockPos);

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
        Direction direction = hitResult.getDirection();
        BlockPos posAtSide = new BlockPos(x + direction.getStepX(), y + direction.getStepY(), z + direction.getStepZ());
        ItemStack stack = player.getItemInHand(hand);
        if (!this.mayPlace(player, direction, stack, posAtSide)) {
            return InteractionResult.FAIL;
        }
        HangingEntity hangingEntity;
        if (this.type == EntityType.PAINTING) {
            hangingEntity = new Painting(level, posAtSide, direction);
        }
        else if (this.type == EntityType.ITEM_FRAME) {
            hangingEntity = new ItemFrame(level, posAtSide, direction);
        }
        else {
            if (this.type != EntityType.GLOW_ITEM_FRAME) {
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            hangingEntity = new GlowItemFrame(level, posAtSide, direction);
        }
        CompoundTag compoundTag = stack.getTag();
        if (compoundTag != null) {
            EntityType.updateCustomEntityTag(level, player, hangingEntity, compoundTag);
        }
        if (hangingEntity.survives()) {
            if (!level.isClientSide) {
                hangingEntity.playPlacementSound();
                level.gameEvent(player, GameEvent.ENTITY_PLACE, new BlockPos(x, y, z));
                level.addFreshEntity(hangingEntity);
            }
            stack.shrink(1);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.CONSUME;
    }
}
