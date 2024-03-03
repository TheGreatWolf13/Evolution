package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.Vanishable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.hooks.asm.DeleteMethod;

@Mixin(TridentItem.class)
public abstract class Mixin_M_TridentItem extends Item implements Vanishable {

    public Mixin_M_TridentItem(Properties properties) {
        super(properties);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public boolean canAttackBlock(BlockState blockState, Level level, BlockPos blockPos, Player player) {
        throw new AbstractMethodError();
    }

    @Override
    public boolean canAttackBlock_(BlockState state, Level level, int x, int y, int z, Player player) {
        return !player.isCreative();
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public boolean mineBlock(ItemStack itemStack, Level level, BlockState blockState, BlockPos blockPos, LivingEntity livingEntity) {
        throw new AbstractMethodError();
    }

    @Override
    public boolean mineBlock_(ItemStack stack, Level level, BlockState state, int x, int y, int z, LivingEntity entity) {
        if (state.getDestroySpeed_() != 0) {
            stack.hurtAndBreak(2, entity, e -> e.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        }
        return true;
    }
}
