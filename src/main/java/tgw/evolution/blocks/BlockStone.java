package tgw.evolution.blocks;

import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.blocks.util.BlockUtils;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionSounds;
import tgw.evolution.util.constants.HarvestLevel;
import tgw.evolution.util.constants.RockVariant;

public class BlockStone extends BlockPhysics implements IRockVariant, IStructural, IFallable, IPoppable {

    private final RockVariant variant;

    public BlockStone(RockVariant variant) {
        super(Properties.of(Material.STONE).strength(variant.getRockType().hardness / 2.0F, 6.0F).sound(SoundType.STONE));
        this.variant = variant;
    }

    @Override
    public boolean canMakeABeamWith(BlockState thisState, BlockState otherState) {
        return otherState.getBlock() instanceof BlockStone;
    }

    @Override
    public boolean canSurvive_(BlockState state, LevelReader level, int x, int y, int z) {
        if (BlockUtils.hasSolidFace(level, x + 1, y, z, Direction.WEST)) {
            return true;
        }
        if (BlockUtils.hasSolidFace(level, x - 1, y, z, Direction.EAST)) {
            return true;
        }
        if (BlockUtils.hasSolidFace(level, x, y + 1, z, Direction.DOWN)) {
            return true;
        }
        if (BlockUtils.hasSolidFace(level, x, y - 1, z, Direction.UP)) {
            return true;
        }
        if (BlockUtils.hasSolidFace(level, x, y, z + 1, Direction.NORTH)) {
            return true;
        }
        return BlockUtils.hasSolidFace(level, x, y, z - 1, Direction.SOUTH);
    }

    @Override
    public @Nullable SoundEvent fallingSound() {
        return EvolutionSounds.STONE_COLLAPSE;
    }

    @Override
    public BeamType getBeamType(BlockState state) {
        return BeamType.CARDINAL_ARCH;
    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return 0.8f;
    }

    @Override
    public int getHarvestLevel(BlockState state, Level level, int x, int y, int z) {
        return HarvestLevel.LOW_METAL;
    }

    @Override
    public int getIncrementForArch(BlockState state) {
        return 4;
    }

    @Override
    public int getIncrementForBeam(BlockState state) {
        return 10;
    }

    @Override
    public int getIntegrity(BlockState state) {
        return this.variant.getRockType().baseIntegrity * 2;
    }

    @Override
    public Stabilization getStabilization(BlockState state) {
        return Stabilization.ARCH;
    }

    @Override
    public BlockState getStateForPhysicsChange(BlockState state) {
        return this.variant.get(EvolutionBlocks.COBBLESTONES).defaultBlockState();
    }

    @Override
    public RockVariant rockVariant() {
        return this.variant;
    }
}
