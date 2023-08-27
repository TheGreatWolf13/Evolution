package tgw.evolution.mixin;

import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.Evolution;
import tgw.evolution.patches.PatchLocationPredicate;

import java.util.Set;

@Mixin(BlockPredicate.class)
public abstract class MixinBlockPredicate implements PatchLocationPredicate {

    @Shadow @Final private @Nullable Set<Block> blocks;
    @Shadow @Final private NbtPredicate nbt;
    @Shadow @Final private StatePropertiesPredicate properties;
    @Shadow @Final private @Nullable TagKey<Block> tag;

    @Overwrite
    public boolean matches(ServerLevel level, BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.matches_(level, pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public boolean matches_(ServerLevel level, int x, int y, int z) {
        if ((Object) this == BlockPredicate.ANY) {
            return true;
        }
        if (!level.isLoaded_(x, y, z)) {
            return false;
        }
        BlockState state = level.getBlockState_(x, y, z);
        if (this.tag != null && !state.is(this.tag)) {
            return false;
        }
        if (this.blocks != null && !this.blocks.contains(state.getBlock())) {
            return false;
        }
        if (!this.properties.matches(state)) {
            return false;
        }
        if (this.nbt != NbtPredicate.ANY) {
            BlockEntity tile = level.getBlockEntity_(x, y, z);
            return tile != null && this.nbt.matches(tile.saveWithFullMetadata());
        }
        return true;
    }
}
