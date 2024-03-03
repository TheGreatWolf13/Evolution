package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;

import java.util.Map;

@Mixin(FlowerPotBlock.class)
public abstract class Mixin_M_FlowerPotBlock extends Block {

    @Shadow @Final protected static VoxelShape SHAPE;
    @Shadow @Final private static Map<Block, Block> POTTED_BY_CONTENT;
    @Shadow @Final private Block content;

    public Mixin_M_FlowerPotBlock(Properties properties) {
        super(properties);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        throw new AbstractMethodError();
    }

    @Override
    public VoxelShape getShape_(BlockState state, BlockGetter level, int x, int y, int z, @Nullable Entity entity) {
        return SHAPE;
    }

    @Shadow
    protected abstract boolean isEmpty();

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public BlockState updateShape(BlockState blockState,
                                  Direction direction,
                                  BlockState blockState2,
                                  LevelAccessor levelAccessor,
                                  BlockPos blockPos,
                                  BlockPos blockPos2) {
        throw new AbstractMethodError();
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
        return from == Direction.DOWN && !state.canSurvive_(level, x, y, z) ?
               Blocks.AIR.defaultBlockState() :
               super.updateShape_(state, from, fromState, level, x, y, z, fromX, fromY, fromZ);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
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
        ItemStack stack = player.getItemInHand(hand);
        Item item = stack.getItem();
        BlockState stateWithFlower = (item instanceof BlockItem b ?
                                      POTTED_BY_CONTENT.getOrDefault(b.getBlock(), Blocks.AIR) :
                                      Blocks.AIR).defaultBlockState();
        boolean invalid = stateWithFlower.is(Blocks.AIR);
        boolean isEmpty = this.isEmpty();
        if (invalid != isEmpty) {
            BlockPos pos = new BlockPos(x, y, z);
            if (isEmpty) {
                level.setBlock(pos, stateWithFlower, 3);
                player.awardStat(Stats.POT_FLOWER);
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
            }
            else {
                ItemStack itemStack2 = new ItemStack(this.content);
                if (stack.isEmpty()) {
                    player.setItemInHand(hand, itemStack2);
                }
                else if (!player.addItem(itemStack2)) {
                    player.drop(itemStack2, false);
                }
                level.setBlock(pos, Blocks.FLOWER_POT.defaultBlockState(), 3);
            }
            level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.CONSUME;
    }
}
