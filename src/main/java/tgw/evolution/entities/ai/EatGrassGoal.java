package tgw.evolution.entities.ai;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.pattern.BlockStateMatcher;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import tgw.evolution.blocks.BlockGrass;
import tgw.evolution.blocks.BlockTallGrass;
import tgw.evolution.capabilities.chunkstorage.ChunkStorageCapability;
import tgw.evolution.entities.AnimalEntity;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.util.EnumFoodNutrients;
import tgw.evolution.util.NutrientHelper;

import java.util.EnumSet;
import java.util.function.Predicate;

public class EatGrassGoal extends Goal {

    private static final Predicate<BlockState> IS_GRASS = BlockStateMatcher.forBlock(EvolutionBlocks.GRASS.get()).or(BlockStateMatcher.forBlock(EvolutionBlocks.TALLGRASS.get()));
    private final AnimalEntity entity;
    private final World world;
    private int eatingGrassTimer;
    private int delay;

    public EatGrassGoal(AnimalEntity grassEaterEntityIn) {
        this.entity = grassEaterEntityIn;
        this.world = grassEaterEntityIn.world;
        this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
        this.delay = 0;
    }

    @Override
    public boolean shouldExecute() {
        if (this.entity.isDead() || this.entity.isSleeping()) {
            return false;
        }
        if (this.delay-- > 0) {
            return false;
        }
        if (this.entity.getRNG().nextInt(this.entity.isChild() ? 50 : 100) != 0) {
            return false;
        }
        BlockPos blockpos = new BlockPos(this.entity);
        if (IS_GRASS.test(this.world.getBlockState(blockpos))) {
            return true;
        }
        return this.world.getBlockState(blockpos.down()).getBlock() instanceof BlockGrass;
    }

    @Override
    public void startExecuting() {
        this.eatingGrassTimer = 40;
        this.world.setEntityState(this.entity, (byte) 10);
        this.entity.getNavigator().clearPath();
    }

    @Override
    public void resetTask() {
        this.eatingGrassTimer = 0;
    }

    @Override
    public boolean shouldContinueExecuting() {
        if (this.entity.isDead()) {
            return false;
        }
        if (this.entity.hurtTime > 0) {
            return false;
        }
        return this.eatingGrassTimer > 0;
    }

    /**
     * Number of ticks since the entity started to eat grass
     */
    public int getEatingGrassTimer() {
        if (!this.shouldContinueExecuting()) {
            this.eatingGrassTimer = 0;
            return 0;
        }
        return this.eatingGrassTimer;
    }

    @Override
    public void tick() {
        this.eatingGrassTimer = Math.max(0, this.eatingGrassTimer - 1);
        if (this.eatingGrassTimer == 4) {
            BlockPos posIn = new BlockPos(this.entity);
            if (IS_GRASS.test(this.world.getBlockState(posIn))) {
                this.world.destroyBlock(posIn, false);
                if (this.world.getBlockState(posIn).getBlock() instanceof BlockTallGrass) {
                    ChunkStorageCapability.getChunkStorage(this.world, this.world.getChunkAt(posIn).getPos()).map(chunkStorage -> {
                        chunkStorage.removeMany(NutrientHelper.DECAY_TALL_GRASS);
                        return true;
                    }).orElseGet(() -> false);
                    this.entity.food.add(EnumFoodNutrients.FOOD, 1);
                    this.entity.food.add(EnumFoodNutrients.PHOSPHORUS, 1);
                    this.entity.food.add(EnumFoodNutrients.POTASSIUM, 1);
                    this.entity.food.add(EnumFoodNutrients.NITROGEN, 1);
                }
                else {
                    ChunkStorageCapability.getChunkStorage(this.world, this.world.getChunkAt(posIn).getPos()).map(chunkStorage -> {
                        chunkStorage.removeMany(NutrientHelper.DECAY_TALL_GRASS);
                        chunkStorage.removeMany(NutrientHelper.DECAY_TALL_GRASS);
                        return true;
                    }).orElseGet(() -> false);
                    this.entity.food.add(EnumFoodNutrients.FOOD, 2);
                    this.entity.food.add(EnumFoodNutrients.PHOSPHORUS, 2);
                    this.entity.food.add(EnumFoodNutrients.POTASSIUM, 2);
                    this.entity.food.add(EnumFoodNutrients.NITROGEN, 2);
                }
                this.delay = 100;
            }
            else {
                BlockPos posDown = posIn.down();
                if (this.world.getBlockState(posDown).getBlock() instanceof BlockGrass) {
                    BlockGrass grassBlock = (BlockGrass) this.world.getBlockState(posDown).getBlock();
                    this.world.playEvent(2001, posDown, Block.getStateId(this.world.getBlockState(posDown)));
                    this.world.setBlockState(posDown, grassBlock.getVariant().getDirt().getDefaultState(), 2);
                    ChunkStorageCapability.getChunkStorage(this.world, this.world.getChunkAt(posIn).getPos()).map(chunkStorage -> {
                        chunkStorage.removeMany(NutrientHelper.DECAY_GRASS_BLOCK);
                        return true;
                    }).orElseGet(() -> false);
                    this.entity.food.add(EnumFoodNutrients.FOOD, 2);
                    this.entity.food.add(EnumFoodNutrients.PHOSPHORUS, 1);
                    this.entity.food.add(EnumFoodNutrients.POTASSIUM, 1);
                    this.entity.food.add(EnumFoodNutrients.NITROGEN, 2);
                    this.delay = 100;
                }
            }
        }
    }
}