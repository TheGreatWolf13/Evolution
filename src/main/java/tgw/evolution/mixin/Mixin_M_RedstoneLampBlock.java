package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.util.constants.BlockFlags;

import java.util.Random;

@Mixin(RedstoneLampBlock.class)
public abstract class Mixin_M_RedstoneLampBlock extends Block {

    @Shadow @Final public static BooleanProperty LIT;

    public Mixin_M_RedstoneLampBlock(Properties properties) {
        super(properties);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
        throw new AbstractMethodError();
    }

    @Override
    public void neighborChanged_(BlockState state,
                                 Level level,
                                 int x,
                                 int y,
                                 int z,
                                 Block oldBlock,
                                 int fromX,
                                 int fromY,
                                 int fromZ,
                                 boolean isMoving) {
        if (!level.isClientSide) {
            boolean lit = state.getValue(LIT);
            BlockPos pos = new BlockPos(x, y, z);
            if (lit != level.hasNeighborSignal(pos)) {
                if (lit) {
                    level.scheduleTick(pos, this, 4);
                }
                else {
                    level.setBlock(pos, state.cycle(LIT), 2);
                }
            }
        }
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        throw new AbstractMethodError();
    }

    @Override
    public void tick_(BlockState state, ServerLevel level, int x, int y, int z, Random random) {
        if (state.getValue(LIT) && !level.hasNeighborSignal(new BlockPos(x, y, z))) {
            level.setBlock_(x, y, z, state.cycle(LIT), BlockFlags.BLOCK_UPDATE);
        }
    }
}
