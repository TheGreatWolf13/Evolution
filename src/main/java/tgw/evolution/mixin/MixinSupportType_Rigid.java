package tgw.evolution.mixin;

import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.patches.PatchSupportType;

@Mixin(targets = "net.minecraft.world.level.block.SupportType$3")
public abstract class MixinSupportType_Rigid implements PatchSupportType {

    @Shadow @Final private VoxelShape RIGID_SUPPORT_SHAPE;

    @Override
    public boolean isSupporting_(BlockState state, BlockGetter level, int x, int y, int z, Direction direction) {
        return !Shapes.joinIsNotEmpty(state.getBlockSupportShape_(level, x, y, z).getFaceShape(direction), this.RIGID_SUPPORT_SHAPE, BooleanOp.ONLY_SECOND);
    }
}
