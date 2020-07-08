package tgw.evolution.world.puzzle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.command.arguments.BlockStateParser;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.gen.feature.template.IStructureProcessorType;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.StructureProcessor;
import net.minecraft.world.gen.feature.template.Template;
import tgw.evolution.init.EvolutionBlocks;

import javax.annotation.Nullable;

public class ProcessorPuzzleReplacement extends StructureProcessor {

    public static final ProcessorPuzzleReplacement INSTANCE = new ProcessorPuzzleReplacement();
    public static final IStructureProcessorType PUZZLE_REPLACEMENT = IStructureProcessorType.register("puzzle_replacement", a -> INSTANCE);

    @Override
    protected IStructureProcessorType getType() {
        return PUZZLE_REPLACEMENT;
    }

    @Override
    protected <T> Dynamic<T> serialize0(DynamicOps<T> ops) {
        return new Dynamic<>(ops, ops.emptyMap());
    }

    @Override
    @Nullable
    public Template.BlockInfo process(IWorldReader worldReaderIn, BlockPos pos, Template.BlockInfo p_215194_3_, Template.BlockInfo blockInfo, PlacementSettings placementSettingsIn) {
        Block block = blockInfo.state.getBlock();
        if (block != EvolutionBlocks.PUZZLE.get()) {
            return blockInfo;
        }
        String s = blockInfo.nbt.getString("FinalState");
        BlockStateParser blockstateparser = new BlockStateParser(new StringReader(s), false);
        try {
            blockstateparser.parse(true);
        }
        catch (CommandSyntaxException commandsyntaxexception) {
            throw new RuntimeException(commandsyntaxexception);
        }
        return blockstateparser.getState().getBlock() == Blocks.STRUCTURE_VOID ? null : new Template.BlockInfo(blockInfo.pos, blockstateparser.getState(), null);
    }
}
