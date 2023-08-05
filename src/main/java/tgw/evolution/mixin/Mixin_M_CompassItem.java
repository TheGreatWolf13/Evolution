package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;

@Mixin(CompassItem.class)
public abstract class Mixin_M_CompassItem extends Item implements Vanishable {

    public Mixin_M_CompassItem(Properties properties) {
        super(properties);
    }

    @Shadow
    protected abstract void addLodestoneTags(ResourceKey<Level> resourceKey,
                                             BlockPos blockPos,
                                             CompoundTag compoundTag);

    @Override
    @Overwrite
    @DeleteMethod
    public InteractionResult useOn(UseOnContext useOnContext) {
        throw new AbstractMethodError();
    }

    @Override
    public InteractionResult useOn_(Level level, int x, int y, int z, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.getBlockState_(x, y, z).is(Blocks.LODESTONE)) {
            return super.useOn_(level, x, y, z, player, hand, hitResult);
        }
        level.playSound(null, x + 0.5, y + 0.5, z + 0.5, SoundEvents.LODESTONE_COMPASS_LOCK, SoundSource.PLAYERS, 1.0F, 1.0F);
        ItemStack stack = player.getItemInHand(hand);
        boolean oneItem = !player.getAbilities().instabuild && stack.getCount() == 1;
        if (oneItem) {
            this.addLodestoneTags(level.dimension(), new BlockPos(x, y, z), stack.getOrCreateTag());
        }
        else {
            ItemStack newStack = new ItemStack(Items.COMPASS, 1);
            //noinspection ConstantConditions
            CompoundTag compoundTag = stack.hasTag() ? stack.getTag().copy() : new CompoundTag();
            newStack.setTag(compoundTag);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            this.addLodestoneTags(level.dimension(), new BlockPos(x, y, z), compoundTag);
            if (!player.getInventory().add(newStack)) {
                player.drop(newStack, false);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
