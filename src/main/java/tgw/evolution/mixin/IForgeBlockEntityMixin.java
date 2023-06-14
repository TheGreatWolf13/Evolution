package tgw.evolution.mixin;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.extensions.IForgeBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.client.renderer.chunk.EvModelDataManager;

@Mixin(IForgeBlockEntity.class)
public interface IForgeBlockEntityMixin {

    /**
     * @author TheGreatWolf
     * @reason Use Evolution ModelManager
     */
    @Overwrite
    default void requestModelDataUpdate() {
        BlockEntity te = this.self();
        Level level = te.getLevel();
        if (level != null && level.isClientSide) {
            EvModelDataManager.requestModelDataRefresh(te);
        }
    }

    @Shadow
    private BlockEntity self() {
        throw new AbstractMethodError();
    }
}
