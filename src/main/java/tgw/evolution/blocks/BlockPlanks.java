package tgw.evolution.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.util.constants.HarvestLevel;
import tgw.evolution.util.constants.WoodVariant;

public class BlockPlanks extends BlockPhysics {

    private final WoodVariant variant;

    public BlockPlanks(WoodVariant variant) {
        super(Properties.of(Material.WOOD).strength(6.0f, 2.0f).sound(SoundType.WOOD));
        this.variant = variant;
    }

//    @Override
//    public int beamSize() {
//        return 5;
//    }

//    @Override
//    public SoundEvent breakSound() {
//        return SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR;
//    }

//    @Override

    @Override
    public @Nullable SoundEvent fallingSound() {
        //TODO implementation
        return null;
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction face) {
        return EvolutionBlocks.FIRE.get().getActualEncouragement(state);
    }
//    public SoundEvent fallSound() {
//        return EvolutionSounds.WOOD_COLLAPSE.get();
//    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction face) {
        return EvolutionBlocks.FIRE.get().getActualFlammability(state);
    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return 0.62f;
    }

    @Override
    public int getHarvestLevel(BlockState state, @Nullable Level level, @Nullable BlockPos pos) {
        return HarvestLevel.STONE;
    }

    @Override
    public double getMass(Level level, BlockPos pos, BlockState state) {
        //TODO implementation
        return 0;
    }

//    @Override
//    public int getShearStrength() {
//        return this.variant.getShearStrength() / 4;
//    }
//
//    @Override
//    public boolean supportCheck(BlockState state) {
//        return state.getBlock() instanceof BlockLog;
//    }
}
