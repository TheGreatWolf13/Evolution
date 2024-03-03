package tgw.evolution.mixin;

import net.minecraft.advancements.critereon.*;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.Evolution;
import tgw.evolution.patches.PatchTriggerInstance;

@Mixin(ItemUsedOnBlockTrigger.TriggerInstance.class)
public abstract class MixinItemUsedOnBlockTriggerInstance extends AbstractCriterionTriggerInstance implements PatchTriggerInstance {

    @Shadow @Final private ItemPredicate item;
    @Shadow @Final private LocationPredicate location;

    public MixinItemUsedOnBlockTriggerInstance(ResourceLocation resourceLocation,
                                               EntityPredicate.Composite composite) {
        super(resourceLocation, composite);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public boolean matches(BlockState state, ServerLevel level, BlockPos pos, ItemStack stack) {
        Evolution.deprecatedMethod();
        return this.matches_(state, level, pos.getX(), pos.getY(), pos.getZ(), stack);
    }

    @Override
    public boolean matches_(BlockState state, ServerLevel level, int x, int y, int z, ItemStack stack) {
        return this.location.matches(level, x + 0.5, y + 0.5, z + 0.5) && this.item.matches(stack);
    }
}
