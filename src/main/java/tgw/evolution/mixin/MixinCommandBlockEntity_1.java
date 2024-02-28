package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.util.constants.BlockFlags;

@Mixin(targets = "net.minecraft.world.level.block.entity.CommandBlockEntity$1")
public abstract class MixinCommandBlockEntity_1 {

    @Shadow(aliases = "this$0") @Final CommandBlockEntity field_11921;

    @Shadow
    public abstract ServerLevel getLevel();

    @Overwrite
    public void onUpdated() {
        BlockPos pos = this.field_11921.getBlockPos();
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        BlockState state = this.getLevel().getBlockState_(x, y, z);
        this.getLevel().sendBlockUpdated_(x, y, z, state, state, BlockFlags.NOTIFY | BlockFlags.BLOCK_UPDATE);
    }
}
