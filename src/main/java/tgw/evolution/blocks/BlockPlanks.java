package tgw.evolution.blocks;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
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
//    public SoundEvent fallSound() {
//        return EvolutionSounds.WOOD_COLLAPSE.get();
//    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return 0.7f;
    }

    @Override
    public int getHarvestLevel(BlockState state, Level level, int x, int y, int z) {
        return HarvestLevel.STONE;
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
