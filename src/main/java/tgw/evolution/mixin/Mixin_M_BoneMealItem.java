package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;

@Mixin(BoneMealItem.class)
public abstract class Mixin_M_BoneMealItem extends Item {

    public Mixin_M_BoneMealItem(Properties properties) {
        super(properties);
    }

    @Shadow
    public static boolean growCrop(ItemStack itemStack, Level level, BlockPos blockPos) {
        throw new AbstractMethodError();
    }

    @Shadow
    public static boolean growWaterPlant(ItemStack itemStack,
                                         Level level,
                                         BlockPos blockPos,
                                         @Nullable Direction direction) {
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
        ItemStack stack = player.getItemInHand(hand);
        if (growCrop(stack, level, new BlockPos(x, y, z))) {
            if (!level.isClientSide) {
                level.levelEvent_(LevelEvent.PARTICLES_AND_SOUND_PLANT_GROWTH, x, y, z, 0);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        Direction direction = hitResult.getDirection();
        boolean solidSide = level.getBlockState_(x, y, z).isFaceSturdy_(level, x, y, z, direction);
        if (solidSide) {
            int sideX = x + direction.getStepX();
            int sideY = y + direction.getStepY();
            int sideZ = z + direction.getStepZ();
            if (growWaterPlant(stack, level, new BlockPos(sideX, sideY, sideZ), direction)) {
                if (!level.isClientSide) {
                    level.levelEvent_(LevelEvent.PARTICLES_AND_SOUND_PLANT_GROWTH, sideX, sideY, sideZ, 0);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return InteractionResult.PASS;
    }
}
