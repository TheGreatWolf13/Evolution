package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Contract;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;

import java.util.Random;
import java.util.random.RandomGenerator;

@Mixin(RespawnAnchorBlock.class)
public abstract class Mixin_M_RespawnAnchorBlock extends Block {

    @Shadow @Final public static IntegerProperty CHARGE;

    public Mixin_M_RespawnAnchorBlock(Properties properties) {
        super(properties);
    }

    @Contract(value = "_ -> _")
    @Shadow
    private static boolean canBeCharged(BlockState blockState) {
        //noinspection Contract
        throw new AbstractMethodError();
    }

    @Contract(value = "_ -> _")
    @Shadow
    public static boolean canSetSpawn(Level level) {
        //noinspection Contract
        throw new AbstractMethodError();
    }

    @Contract(value = "_, _, _ -> _")
    @Shadow
    public static void charge(Level level, BlockPos blockPos, BlockState blockState) {
        //noinspection Contract
        throw new AbstractMethodError();
    }

    @Contract(value = "_ -> _")
    @Shadow
    private static boolean isRespawnFuel(ItemStack itemStack) {
        //noinspection Contract
        throw new AbstractMethodError();
    }

    @Override
    @Overwrite
    @DeleteMethod
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
        throw new AbstractMethodError();
    }

    @Override
    public void animateTick_(BlockState state, Level level, int x, int y, int z, RandomGenerator random) {
        if (state.getValue(CHARGE) != 0) {
            if (random.nextInt(100) == 0) {
                level.playSound(null, x + 0.5, y + 0.5, z + 0.5, SoundEvents.RESPAWN_ANCHOR_AMBIENT, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            level.addParticle(ParticleTypes.REVERSE_PORTAL,
                              x + 0.5 + (0.5 - random.nextDouble()), y + 1, z + 0.5 + (0.5 - random.nextDouble()),
                              0, random.nextFloat() * 0.04, 0
            );
        }
    }

    @Shadow
    protected abstract void explode(BlockState blockState, Level level, BlockPos blockPos);

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
        ItemStack itemStack = player.getItemInHand(hand);
        if (hand == InteractionHand.MAIN_HAND &&
            !isRespawnFuel(itemStack) &&
            isRespawnFuel(player.getItemInHand(InteractionHand.OFF_HAND))) {
            return InteractionResult.PASS;
        }
        if (isRespawnFuel(itemStack) && canBeCharged(state)) {
            charge(level, new BlockPos(x, y, z), state);
            if (!player.getAbilities().instabuild) {
                itemStack.shrink(1);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (state.getValue(CHARGE) == 0) {
            return InteractionResult.PASS;
        }
        if (!canSetSpawn(level)) {
            if (!level.isClientSide) {
                this.explode(state, level, new BlockPos(x, y, z));
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!level.isClientSide) {
            ServerPlayer serverPlayer = (ServerPlayer) player;
            BlockPos respawnPosition;
            if (serverPlayer.getRespawnDimension() != level.dimension() ||
                (respawnPosition = serverPlayer.getRespawnPosition()) == null ||
                respawnPosition.getX() != x ||
                respawnPosition.getY() != y ||
                respawnPosition.getZ() != z) {
                serverPlayer.setRespawnPosition(level.dimension(), new BlockPos(x, y, z), 0.0F, false, true);
                level.playSound(null, x + 0.5, y + 0.5, z + 0.5, SoundEvents.RESPAWN_ANCHOR_SET_SPAWN, SoundSource.BLOCKS, 1.0F, 1.0F);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.CONSUME;
    }
}
