package tgw.evolution.blocks;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import tgw.evolution.init.EvolutionSounds;
import tgw.evolution.util.constants.HarvestLevel;
import tgw.evolution.util.constants.WoodVariant;

public class BlockLog extends BlockXYZAxis implements IWoodVariant {

    private final WoodVariant variant;

    public BlockLog(WoodVariant variant) {
        super(Properties.of(Material.WOOD).strength(8.0F, 2.0F).sound(SoundType.WOOD), variant.getMass());
        this.variant = variant;
    }

    @Override
    public SoundEvent fallingSound() {
        return EvolutionSounds.WOOD_COLLAPSE;
    }

//    @Override
//    public boolean beamCondition(BlockState checking, BlockState state) {
//        return state.getValue(AXIS) == checking.getValue(AXIS);
//    }

//    @Override
//    public Direction[] beamDirections(BlockState state) {
//        return new Direction[]{MathHelper.getNegativeAxis(state.getValue(AXIS)), MathHelper.getPositiveAxis(state.getValue(AXIS))};
//    }

//    @Override
//    public int beamSize() {
//        return 8;
//    }

//    @Override
//    public SoundEvent breakSound() {
//        return EvolutionSounds.WOOD_BREAK.get();
//    }

//    @Override
//    protected boolean canSustainWeight(BlockState state) {
//        return state.getValue(AXIS) != Direction.Axis.Y && super.canSustainWeight(state);
//    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return 0.7f;
    }

    @Override
    public int getHarvestLevel(BlockState state, Level level, int x, int y, int z) {
        return HarvestLevel.STONE;
    }

    @Override
    public double getMass(Level level, int x, int y, int z, BlockState state) {
        return this.woodVariant().getMass();
    }

    @Override
    public WoodVariant woodVariant() {
        return this.variant;
    }

//    @Override
//    public int getShearStrength() {
//        return this.variant.getShearStrength();
//    }
}
