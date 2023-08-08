package tgw.evolution.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import tgw.evolution.blocks.tileentities.TETorch;
import tgw.evolution.blocks.util.BlockUtils;
import tgw.evolution.config.EvolutionConfig;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.init.EvolutionShapes;
import tgw.evolution.items.ItemTorch;
import tgw.evolution.util.time.Time;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.random.RandomGenerator;

import static tgw.evolution.init.EvolutionBStates.LIT;

public class BlockTorch extends BlockPhysics implements IReplaceable, IFireSource, EntityBlock, IPoppable, IAir {

    public BlockTorch() {
        super(Properties.of(Material.DECORATION).strength(0.0F).randomTicks().noDrops().noCollission().sound(SoundType.WOOD));
        this.registerDefaultState(this.defaultBlockState().setValue(LIT, true));
    }

    @Override
    public boolean allowsFrom(BlockState state, Direction from) {
        return true;
    }

    @Override
    public void animateTick_(BlockState state, Level level, int x, int y, int z, RandomGenerator random) {
        if (!state.getValue(LIT)) {
            return;
        }
        double posX = x + 0.5;
        double posY = y + 0.7;
        double posZ = z + 0.5;
        level.addParticle(ParticleTypes.SMOKE, posX, posY, posZ, 0, 0, 0);
        level.addParticle(ParticleTypes.FLAME, posX, posY, posZ, 0, 0, 0);
    }

    @Override
    public boolean canBeReplacedByFluid(BlockState state) {
        return true;
    }

    @Override
    public boolean canBeReplacedByRope(BlockState state) {
        return true;
    }

    @Override
    public boolean canSurvive_(BlockState state, LevelReader level, int x, int y, int z) {
        return BlockUtils.canSupportCenter(level, x, y - 1, z, Direction.UP);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        if (!state.getValue(LIT)) {
            return Collections.singletonList(new ItemStack(EvolutionItems.TORCH_UNLIT));
        }
        BlockEntity tile = builder.getParameter(LootContextParams.BLOCK_ENTITY);
        if (tile instanceof TETorch teTorch) {
            return Collections.singletonList(ItemTorch.getDroppedStack(teTorch));
        }
        return Collections.singletonList(ItemStack.EMPTY);
    }

    @Override
    public double getMass(Level level, int x, int y, int z, BlockState state) {
        return 0;
    }

    @Override
    public VoxelShape getShape_(BlockState state, BlockGetter level, int x, int y, int z, @Nullable Entity entity) {
        return EvolutionShapes.TORCH;
    }

    @Override
    public @Nullable BlockState getStateForPlacement_(Level level,
                                                      int x,
                                                      int y,
                                                      int z,
                                                      Player player,
                                                      InteractionHand hand,
                                                      BlockHitResult hitResult) {
        if (!level.getFluidState_(x, y, z).isEmpty()) {
            return this.defaultBlockState().setValue(LIT, false);
        }
        return this.defaultBlockState();
    }

    @Override
    public @Range(from = 1, to = 31) int increment(BlockState state, Direction from) {
        return 1;
    }

    @Override
    public boolean isFireSource(BlockState state) {
        return state.getValue(LIT);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return state.getValue(LIT);
    }

    @Override
    public boolean isReplaceable(BlockState state) {
        return true;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TETorch(pos, state);
    }

    @Override
    public void onPlace_(BlockState state, Level level, int x, int y, int z, BlockState oldState, boolean isMoving) {
        if (level.isClientSide) {
            return;
        }
        if (!state.getValue(LIT)) {
            return;
        }
        if (level.getBlockEntity_(x, y, z) instanceof TETorch te) {
            te.setPlaceTime();
        }
    }

    @Override
    public void randomTick_(BlockState state, ServerLevel level, int x, int y, int z, Random random) {
        if (!state.getValue(LIT)) {
            return;
        }
        BlockEntity tile = level.getBlockEntity_(x, y, z);
        if (tile instanceof TETorch teTorch) {
            int torchTime = EvolutionConfig.SERVER.torchTime.get();
            if (torchTime == 0) {
                return;
            }
            if (level.getDayTime() >= teTorch.getTimePlaced() + (long) torchTime * Time.TICKS_PER_HOUR) {
                level.setBlockAndUpdate_(x, y, z, state.setValue(LIT, false));
            }
        }
    }

    @Override
    public BlockState updateShape_(BlockState state,
                                   Direction from,
                                   BlockState fromState,
                                   LevelAccessor level,
                                   int x,
                                   int y,
                                   int z,
                                   int fromX,
                                   int fromY,
                                   int fromZ) {
        return from == Direction.DOWN && !this.canSurvive_(state, level, x, y, z) ?
               Blocks.AIR.defaultBlockState() :
               super.updateShape_(state, from, fromState, level, x, y, z, fromX, fromY, fromZ);
    }

    @Override
    public InteractionResult use_(BlockState state, Level level, int x, int y, int z, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.isEmpty() && state.getValue(LIT)) {
            level.setBlockAndUpdate(new BlockPos(x, y, z), state.setValue(LIT, false));
            level.playSound(player,
                            x + 0.5, y + 0.5, z + 0.5,
                            SoundEvents.FIRE_EXTINGUISH,
                            SoundSource.BLOCKS,
                            1.0F,
                            2.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.8F);
            return InteractionResult.SUCCESS;
        }
        if (stack.getItem() == EvolutionItems.TORCH && !state.getValue(LIT)) {
            level.setBlockAndUpdate(new BlockPos(x, y, z), state.setValue(LIT, true));
            level.playSound(player, x + 0.5, y + 0.5, z + 0.5, SoundEvents.FIRE_AMBIENT, SoundSource.BLOCKS, 1.0F,
                            level.random.nextFloat() * 0.7F + 0.3F);
            if (!level.isClientSide) {
                if (level.getBlockEntity_(x, y, z) instanceof TETorch te) {
                    te.setPlaceTime();
                }
            }
            player.awardStat(Stats.ITEM_CRAFTED.get(EvolutionItems.TORCH));
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}	
