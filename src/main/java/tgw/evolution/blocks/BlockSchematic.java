package tgw.evolution.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.blocks.tileentities.SchematicMode;
import tgw.evolution.blocks.tileentities.TESchematic;

import static tgw.evolution.init.EvolutionBStates.SCHEMATIC_MODE;

public class BlockSchematic extends Block implements GameMasterBlock, EntityBlock {

    public BlockSchematic() {
        super(Properties.of(Material.METAL, MaterialColor.COLOR_LIGHT_GRAY).strength(-1.0F, 3_600_000.0F).sound(SoundType.METAL).noDrops());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SCHEMATIC_MODE);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(SCHEMATIC_MODE, SchematicMode.SAVE);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TESchematic(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity tileentity = level.getBlockEntity(pos);
        return tileentity instanceof TESchematic teSchematic && teSchematic.usedBy(player) ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }
}
