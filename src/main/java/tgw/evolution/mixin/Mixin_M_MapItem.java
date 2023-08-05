package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ComplexItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;

@Mixin(MapItem.class)
public abstract class Mixin_M_MapItem extends ComplexItem {

    public Mixin_M_MapItem(Properties properties) {
        super(properties);
    }

    @Shadow
    @Contract(value = "_, _ -> _")
    public static @Nullable MapItemSavedData getSavedData(ItemStack itemStack, Level level) {
        //noinspection Contract
        throw new AbstractMethodError();
    }

    @Override
    @Overwrite
    @DeleteMethod
    public InteractionResult useOn(UseOnContext useOnContext) {
        throw new AbstractMethodError();
    }

    @Override
    public InteractionResult useOn_(Level level, int x, int y, int z, Player player, InteractionHand hand, BlockHitResult hitResult) {
        BlockState state = level.getBlockState_(x, y, z);
        if (state.is(BlockTags.BANNERS)) {
            if (!level.isClientSide) {
                MapItemSavedData mapItemSavedData = getSavedData(player.getItemInHand(hand), level);
                if (mapItemSavedData != null && !mapItemSavedData.toggleBanner(level, new BlockPos(x, y, z))) {
                    return InteractionResult.FAIL;
                }
            }

            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.useOn_(level, x, y, z, player, hand, hitResult);
    }
}
