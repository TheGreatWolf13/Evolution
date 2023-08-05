package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ComparatorMode;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;

import java.util.Random;

@Mixin(ComparatorBlock.class)
public abstract class Mixin_M_ComparatorBlock extends DiodeBlock implements EntityBlock {

    @Shadow @Final public static EnumProperty<ComparatorMode> MODE;

    public Mixin_M_ComparatorBlock(Properties properties) {
        super(properties);
    }

    @Shadow
    protected abstract void refreshOutputState(Level level, BlockPos blockPos, BlockState blockState);

    @Override
    @Overwrite
    @DeleteMethod
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        throw new AbstractMethodError();
    }

    @Override
    public void tick_(BlockState state, ServerLevel level, int x, int y, int z, Random random) {
        this.refreshOutputState(level, new BlockPos(x, y, z), state);
    }

    @Override
    @Overwrite
    @DeleteMethod
    public InteractionResult use(BlockState blockState,
                                 Level level,
                                 BlockPos blockPos,
                                 Player player,
                                 InteractionHand interactionHand,
                                 BlockHitResult blockHitResult) {
        throw new AbstractMethodError();
    }

    @Override
    public InteractionResult use_(BlockState state, Level level, int x, int y, int z, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!player.getAbilities().mayBuild) {
            return InteractionResult.PASS;
        }
        state = state.cycle(MODE);
        float pitch = state.getValue(MODE) == ComparatorMode.SUBTRACT ? 0.55F : 0.5F;
        level.playSound(player, x + 0.5, y + 0.5, z + 0.5, SoundEvents.COMPARATOR_CLICK, SoundSource.BLOCKS, 0.3F, pitch);
        BlockPos pos = new BlockPos(x, y, z);
        level.setBlock(pos, state, 2);
        this.refreshOutputState(level, pos, state);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
