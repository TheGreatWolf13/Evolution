package tgw.evolution.mixin;

import it.unimi.dsi.fastutil.objects.ReferenceSet;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(BlockEntityType.Builder.class)
public abstract class MixinBlockEntityType_Builder<T extends BlockEntity> {

    /**
     * @author TheGreatWolf
     * @reason Use faster set
     */
    @SuppressWarnings("OverwriteModifiers")
    @Overwrite
    public static <T extends BlockEntity> BlockEntityType.Builder<T> of(BlockEntityType.BlockEntitySupplier<? extends T> factory,
                                                                        Block... validBlocks) {
        return new BlockEntityType.Builder<>(factory, ReferenceSet.of(validBlocks));
    }
}
