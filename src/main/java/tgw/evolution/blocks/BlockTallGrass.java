package tgw.evolution.blocks;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.init.EvolutionShapes;

public class BlockTallGrass extends BlockBush {

    public BlockTallGrass() {
        super(Properties.of(Material.PLANT).noCollission().noDrops().strength(0.0F).sound(SoundType.GRASS));
    }

    @Override
    public OffsetType getOffsetType() {
        return OffsetType.XYZ;
    }

    @Override
    public VoxelShape getShape_(BlockState state, BlockGetter level, int x, int y, int z, @Nullable Entity entity) {
        return EvolutionShapes.GRASS;
    }
}
