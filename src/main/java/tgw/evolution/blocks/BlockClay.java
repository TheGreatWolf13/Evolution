package tgw.evolution.blocks;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.init.EvolutionSounds;

import java.util.Random;
import java.util.function.Consumer;

public class BlockClay extends BlockPhysics implements IStructural, IFallable, IGrassSpreadable {

    public BlockClay() {
        super(Properties.of(Material.CLAY).strength(2.0F, 0.6F).sound(SoundType.GRAVEL));
    }

    @Override
    public boolean canMakeABeamWith(BlockState thisState, BlockState otherState) {
        Block block = otherState.getBlock();
        return block == EvolutionBlocks.CLAY || block == EvolutionBlocks.GRASS_CLAY;
    }

    @Override
    public void dropLoot(BlockState state, ServerLevel level, int x, int y, int z, ItemStack tool, @Nullable BlockEntity tile, @Nullable Entity entity, Random random, Consumer<ItemStack> consumer) {
        consumer.accept(new ItemStack(EvolutionItems.CLAYBALL, 8));
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
    public BlockGenericSpreadable getGrass() {
        return EvolutionBlocks.GRASS_CLAY;
    }

    @Override
    public int getIntegrity(BlockState state) {
        return 1;
    }

    @Override
    public Stabilization getStabilization(BlockState state) {
        return Stabilization.BEAM;
    }
}
