package tgw.evolution.world.puzzle.pieces;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.mojang.datafixers.Dynamic;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.nbt.StringNBT;
import net.minecraft.state.properties.StructureMode;
import net.minecraft.util.IDynamicDeserializer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.feature.template.*;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.util.NBTHelper;
import tgw.evolution.world.puzzle.EnumPuzzleType;
import tgw.evolution.world.puzzle.ProcessorPuzzleReplacement;
import tgw.evolution.world.puzzle.PuzzlePattern;
import tgw.evolution.world.puzzle.PuzzlePiece;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SinglePuzzlePiece extends PuzzlePiece {

    protected final ResourceLocation location;
    protected final ImmutableList<StructureProcessor> processors;

    public SinglePuzzlePiece(String location, List<StructureProcessor> processors) {
        this(location, processors, PuzzlePattern.PlacementBehaviour.RIGID);
    }

    public SinglePuzzlePiece(String location, List<StructureProcessor> processors, PuzzlePattern.PlacementBehaviour placementBehaviour) {
        super(placementBehaviour);
        this.location = new ResourceLocation(location);
        this.processors = ImmutableList.copyOf(processors);
    }

    public SinglePuzzlePiece(String location) {
        this(location, ImmutableList.of());
    }

    public SinglePuzzlePiece(INBT nbt) {
        super(nbt);
        this.location = new ResourceLocation(NBTHelper.asString(nbt, "location", ""));
        this.processors = ImmutableList.copyOf(NBTHelper.asList(nbt, "processors", inbt -> IDynamicDeserializer.func_214907_a(new Dynamic<>(NBTDynamicOps.INSTANCE, inbt), Registry.STRUCTURE_PROCESSOR, "processor_type", NopProcessor.INSTANCE)));
    }

    public List<Template.BlockInfo> func_214857_a(TemplateManager manager, BlockPos pos, Rotation rotation, boolean p_214857_4_) {
        Template template = manager.getTemplateDefaulted(this.location);
        List<Template.BlockInfo> list = template.func_215386_a(pos, new PlacementSettings().setRotation(rotation), Blocks.STRUCTURE_BLOCK, p_214857_4_);
        List<Template.BlockInfo> list1 = Lists.newArrayList();
        for (Template.BlockInfo blockInfo : list) {
            if (blockInfo.nbt != null) {
                StructureMode structuremode = StructureMode.valueOf(blockInfo.nbt.getString("mode"));
                if (structuremode == StructureMode.DATA) {
                    list1.add(blockInfo);
                }
            }
        }
        return list1;
    }

    @Override
    public List<Template.BlockInfo> getPuzzleBlocks(TemplateManager manager, BlockPos pos, Rotation rotation, Random rand) {
        Template template = manager.getTemplateDefaulted(this.location);
        List<Template.BlockInfo> list = template.func_215386_a(pos, new PlacementSettings().setRotation(rotation), EvolutionBlocks.PUZZLE.get(), true);
        Collections.shuffle(list, rand);
        return list;
    }

    @Override
    public MutableBoundingBox getBoundingBox(TemplateManager templateManagerIn, BlockPos pos, Rotation rotationIn) {
        Template template = templateManagerIn.getTemplateDefaulted(this.location);
        return template.getMutableBoundingBox(new PlacementSettings().setRotation(rotationIn), pos);
    }

    @Override
    public boolean place(TemplateManager templateManagerIn, IWorld worldIn, BlockPos pos, Rotation rotationIn, MutableBoundingBox boundsIn, Random rand) {
        Template template = templateManagerIn.getTemplateDefaulted(this.location);
        PlacementSettings placementsettings = this.createPlacementSettings(rotationIn, boundsIn);
        if (!template.addBlocksToWorld(worldIn, pos, placementsettings, 18)) {
            return false;
        }
        for (Template.BlockInfo blockInfo : Template.processBlockInfos(template, worldIn, pos, placementsettings, this.func_214857_a(templateManagerIn, pos, rotationIn, false))) {
            this.func_214846_a(worldIn, blockInfo, pos, rotationIn, rand, boundsIn);
        }
        return true;
    }

    @Override
    public EnumPuzzleType getType() {
        return EnumPuzzleType.SINGLE;
    }

    protected PlacementSettings createPlacementSettings(Rotation rotationIn, MutableBoundingBox boundsIn) {
        PlacementSettings placementsettings = new PlacementSettings();
        placementsettings.setBoundingBox(boundsIn);
        placementsettings.setRotation(rotationIn);
        placementsettings.func_215223_c(true);
        placementsettings.setIgnoreEntities(false);
        placementsettings.addProcessor(BlockIgnoreStructureProcessor.STRUCTURE_BLOCK);
        placementsettings.addProcessor(ProcessorPuzzleReplacement.INSTANCE);
        this.processors.forEach(placementsettings::addProcessor);
        this.getPlacementBehaviour().getStructureProcessors().forEach(placementsettings::addProcessor);
        return placementsettings;
    }

    @Override
    public INBT serialize0() {
        return NBTHelper.createMap(ImmutableMap.of(new StringNBT("location"), new StringNBT(this.location.toString()), new StringNBT("processors"), NBTHelper.createList(this.processors.stream().map(processor -> processor.serialize(NBTDynamicOps.INSTANCE).getValue()))));
    }

    @Override
    public String toString() {
        return "SinglePuzzlePiece[" + this.location + "]";
    }

    @Override
    public boolean equals(Object o) {
        return this == o;
    }

    @Override
    public int hashCode() {
        return this.location.hashCode();
    }
}
