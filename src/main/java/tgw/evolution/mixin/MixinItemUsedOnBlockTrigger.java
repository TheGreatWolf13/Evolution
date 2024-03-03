package tgw.evolution.mixin;

import net.minecraft.advancements.critereon.ItemUsedOnBlockTrigger;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.Evolution;
import tgw.evolution.patches.PatchSimpleCriteria;

@Mixin(ItemUsedOnBlockTrigger.class)
public abstract class MixinItemUsedOnBlockTrigger extends SimpleCriterionTrigger<ItemUsedOnBlockTrigger.TriggerInstance> implements
                                                                                                                         PatchSimpleCriteria {

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public void trigger(ServerPlayer player, BlockPos pos, ItemStack stack) {
        Evolution.deprecatedMethod();
        this.trigger_(player, pos.getX(), pos.getY(), pos.getZ(), stack);
    }

    @Override
    public void trigger_(ServerPlayer player, int x, int y, int z, ItemStack stack) {
        BlockState state = player.getLevel().getBlockState_(x, y, z);
        this.trigger(player, t -> t.matches_(state, player.getLevel(), x, y, z, stack));
    }
}
