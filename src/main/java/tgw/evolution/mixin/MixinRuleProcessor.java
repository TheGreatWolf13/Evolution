package tgw.evolution.mixin;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Random;

@Mixin(RuleProcessor.class)
public abstract class MixinRuleProcessor extends StructureProcessor {

    @Shadow @Final private ImmutableList<ProcessorRule> rules;

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public @Nullable StructureTemplate.StructureBlockInfo processBlock(LevelReader level, BlockPos blockPos, BlockPos blockPos2, StructureTemplate.StructureBlockInfo structureBlockInfo, StructureTemplate.StructureBlockInfo structureBlockInfo2, StructurePlaceSettings structurePlaceSettings) {
        ImmutableList<ProcessorRule> rules = this.rules;
        if (rules.isEmpty()) {
            return structureBlockInfo2;
        }
        Random random = new Random(Mth.getSeed(structureBlockInfo2.pos));
        BlockState blockState = level.getBlockState_(structureBlockInfo2.pos);
        for (int i = 0, len = rules.size(); i < len; ++i) {
            ProcessorRule processorRule = rules.get(i);
            if (processorRule.test(structureBlockInfo2.state, blockState, structureBlockInfo.pos, structureBlockInfo2.pos, blockPos2, random)) {
                return new StructureTemplate.StructureBlockInfo(structureBlockInfo2.pos, processorRule.getOutputState(), processorRule.getOutputTag());
            }
        }
        return structureBlockInfo2;
    }
}
