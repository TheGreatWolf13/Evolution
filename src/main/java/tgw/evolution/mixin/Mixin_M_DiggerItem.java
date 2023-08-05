package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.hooks.asm.DeleteMethod;

@Mixin(DiggerItem.class)
public abstract class Mixin_M_DiggerItem extends TieredItem implements Vanishable {

    public Mixin_M_DiggerItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    @Overwrite
    @DeleteMethod
    public boolean mineBlock(ItemStack itemStack, Level level, BlockState blockState, BlockPos blockPos, LivingEntity livingEntity) {
        throw new AbstractMethodError();
    }

    @Override
    public boolean mineBlock_(ItemStack stack, Level level, BlockState state, int x, int y, int z, LivingEntity entity) {
        if (!level.isClientSide && state.getDestroySpeed_() != 0.0F) {
            stack.hurtAndBreak(1, entity, e -> e.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        }
        return true;
    }
}
