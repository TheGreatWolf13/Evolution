package tgw.evolution.blocks;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.capabilities.chunk.ChunkAllowance;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionSounds;
import tgw.evolution.util.constants.NutrientVariant;

public class BlockGrass extends BlockGenericSpreadable implements INutrientVariant, IStructural, IFallable {

    private final NutrientVariant variant;

    public BlockGrass(NutrientVariant variant) {
        super(Properties.of(Material.DIRT).color(MaterialColor.GRASS).strength(3.0F, 0.6F).sound(SoundType.GRASS));
        this.variant = variant;
    }

    @Override
    public boolean canMakeABeamWith(BlockState thisState, BlockState otherState) {
        return otherState.getBlock() instanceof BlockGrass;
    }

    @Override
    public BlockState deadBlockState() {
        return this.variant.get(EvolutionBlocks.DIRTS).defaultBlockState();
    }

    @Override
    public @Nullable SoundEvent fallingSound() {
        return EvolutionSounds.SOIL_COLLAPSE;
    }

    @Override
    public BeamType getBeamType(BlockState state) {
        return BeamType.CARDINAL_BEAM;
    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return 0.6F;
    }

    @Override
    public int getIntegrity(BlockState state) {
        return 1;
    }

    @Override
    public Stabilization getStabilization(BlockState state) {
        return Stabilization.NONE;
    }

    @Override
    public NutrientVariant nutrientVariant() {
        return this.variant;
    }

    @Override
    protected int getTallGrassAllowanceCost() {
        return switch (this.variant) {
            case RICH -> ChunkAllowance.BASE_GRASS_COST;
            case NORMAL -> 2 * ChunkAllowance.BASE_GRASS_COST;
            case POOR -> 4 * ChunkAllowance.BASE_GRASS_COST;
        };
    }
}
