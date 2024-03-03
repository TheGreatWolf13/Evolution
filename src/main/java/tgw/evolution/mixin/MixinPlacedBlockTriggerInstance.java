package tgw.evolution.mixin;

import net.minecraft.advancements.critereon.*;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.Evolution;
import tgw.evolution.patches.PatchTriggerInstance;

@Mixin(PlacedBlockTrigger.TriggerInstance.class)
public abstract class MixinPlacedBlockTriggerInstance extends AbstractCriterionTriggerInstance implements PatchTriggerInstance {

    @Shadow @Final private @Nullable Block block;
    @Shadow @Final private ItemPredicate item;
    @Shadow @Final private LocationPredicate location;
    @Shadow @Final private StatePropertiesPredicate state;

    public MixinPlacedBlockTriggerInstance(ResourceLocation resourceLocation,
                                           EntityPredicate.Composite composite) {
        super(resourceLocation, composite);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public boolean matches(BlockState state, BlockPos pos, ServerLevel level, ItemStack stack) {
        Evolution.deprecatedMethod();
        return this.matches_(state, level, pos.getX(), pos.getY(), pos.getZ(), stack);
    }

    @Override
    public boolean matches_(BlockState state, ServerLevel level, int x, int y, int z, ItemStack stack) {
        if (this.block != null && !state.is(this.block)) {
            return false;
        }
        if (!this.state.matches(state)) {
            return false;
        }
        if (!this.location.matches(level, x, y, z)) {
            return false;
        }
        return this.item.matches(stack);
    }
}
