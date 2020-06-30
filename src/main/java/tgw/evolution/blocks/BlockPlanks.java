package tgw.evolution.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionSounds;
import tgw.evolution.util.EnumWoodNames;
import tgw.evolution.util.HarvestLevel;

public class BlockPlanks extends BlockGravity {

    private final EnumWoodNames variant;

    public BlockPlanks(EnumWoodNames variant) {
        super(Block.Properties.create(Material.WOOD).hardnessAndResistance(6f, 2f).sound(SoundType.WOOD).harvestLevel(HarvestLevel.STONE), variant.getMass() / 4);
        this.variant = variant;
    }

    @Override
    public int getFlammability(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
        return ((BlockFire) EvolutionBlocks.FIRE.get()).getActualFlammability(state);
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
        return ((BlockFire) EvolutionBlocks.FIRE.get()).getActualEncouragement(state);
    }

    @Override
    public boolean supportCheck(BlockState state) {
        return state.getBlock() instanceof BlockLog;
    }

    @Override
    public int getShearStrength() {
        return this.variant.getShearStrength() / 4;
    }

    @Override
    public int beamSize() {
        return 5;
    }

    @Override
    public SoundEvent breakSound() {
        return SoundEvents.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR;
    }

    @Override
    public SoundEvent fallSound() {
        return EvolutionSounds.WOOD_COLLAPSE.get();
    }
}
