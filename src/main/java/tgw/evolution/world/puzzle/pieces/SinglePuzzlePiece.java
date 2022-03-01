//package tgw.evolution.world.puzzle.pieces;
//
//import com.google.common.collect.ImmutableList;
//import com.google.common.collect.ImmutableMap;
//import com.mojang.datafixers.Dynamic;
//import net.minecraft.nbt.INBT;
//import net.minecraft.nbt.NBTDynamicOps;
//import net.minecraft.nbt.StringNBT;
//import net.minecraft.util.ResourceLocation;
//import net.minecraft.util.Rotation;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.util.math.MutableBoundingBox;
//import net.minecraft.util.registry.DynamicRegistries;
//import net.minecraft.util.registry.Registry;
//import net.minecraft.world.IWorld;
//import net.minecraft.world.gen.feature.template.*;
//import tgw.evolution.init.EvolutionBlocks;
//import tgw.evolution.util.constants.NBTHelper;
//import tgw.evolution.world.puzzle.EnumPuzzleType;
//import tgw.evolution.world.puzzle.ProcessorPuzzleReplacement;
//import tgw.evolution.world.puzzle.PuzzlePiece;
//import tgw.evolution.world.puzzle.pieces.config.PlacementType;
//
//import java.util.Collections;
//import java.util.List;
//import java.util.Random;
//
//public class SinglePuzzlePiece extends PuzzlePiece {
//
//    protected final ResourceLocation location;
//    protected final ImmutableList<StructureProcessor> processors;
//
//    public SinglePuzzlePiece(String location, List<StructureProcessor> processors) {
//        this(location, processors, PlacementType.RIGID);
//    }
//
//    public SinglePuzzlePiece(String location, List<StructureProcessor> processors, PlacementType placementBehaviour) {
//        super(placementBehaviour);
//        this.location = new ResourceLocation(location);
//        this.processors = ImmutableList.copyOf(processors);
//    }
//
//    public SinglePuzzlePiece(String location) {
//        this(location, ImmutableList.of());
//    }
//
//    public SinglePuzzlePiece(INBT nbt) {
//        super(nbt);
//        this.location = new ResourceLocation(NBTHelper.asString(nbt, "Loc", ""));
//        this.processors = ImmutableList.copyOf(NBTHelper.asList(nbt,
//                                                                "Proc",
//                                                                inbt -> DynamicRegistries.func_214907_a(new Dynamic<>(NBTDynamicOps.INSTANCE, inbt),
//                                                                                                        Registry.STRUCTURE_PROCESSOR,
//                                                                                                        "processor_type",
//                                                                                                        NopProcessor.INSTANCE)));
//    }
//
//    protected PlacementSettings createPlacementSettings(Rotation rotationIn, MutableBoundingBox boundsIn) {
//        PlacementSettings placementsettings = new PlacementSettings();
//        placementsettings.setBoundingBox(boundsIn);
//        placementsettings.setRotation(rotationIn);
//        placementsettings.func_215223_c(true);
//        placementsettings.setIgnoreEntities(false);
//        placementsettings.addProcessor(BlockIgnoreStructureProcessor.STRUCTURE_BLOCK);
//        placementsettings.addProcessor(ProcessorPuzzleReplacement.INSTANCE);
//        this.processors.forEach(placementsettings::addProcessor);
//        this.getPlacementBehaviour().getStructureProcessors().forEach(placementsettings::addProcessor);
//        return placementsettings;
//    }
//
//    @Override
//    public boolean equals(Object o) {
//        if (!(o instanceof SinglePuzzlePiece)) {
//            return false;
//        }
//        if (!super.equals(o)) {
//            return false;
//        }
//        SinglePuzzlePiece that = (SinglePuzzlePiece) o;
//        return this.location.equals(that.location);
//    }
//
//    @Override
//    public MutableBoundingBox getBoundingBox(TemplateManager templateManagerIn, BlockPos pos, Rotation rotationIn) {
//        Template template = templateManagerIn.getTemplateDefaulted(this.location);
//        return template.getMutableBoundingBox(new PlacementSettings().setRotation(rotationIn), pos);
//    }
//
//    @Override
//    public List<Template.BlockInfo> getPuzzleBlocks(TemplateManager manager, BlockPos pos, Rotation rotation, Random rand) {
//        Template template = manager.getTemplateDefaulted(this.location);
//        List<Template.BlockInfo> list = template.func_215386_a(pos,
//                                                               new PlacementSettings().setRotation(rotation),
//                                                               EvolutionBlocks.PUZZLE.get(),
//                                                               true);
//        Collections.shuffle(list, rand);
//        return list;
//    }
//
//    @Override
//    public EnumPuzzleType getType() {
//        return EnumPuzzleType.SINGLE;
//    }
//
//    @Override
//    public int hashCode() {
//        return this.location.hashCode();
//    }
//
//    @Override
//    public boolean place(TemplateManager templateManagerIn,
//                         IWorld worldIn,
//                         BlockPos pos,
//                         Rotation rotationIn,
//                         MutableBoundingBox boundsIn,
//                         Random rand) {
//        Template template = templateManagerIn.getTemplateDefaulted(this.location);
//        PlacementSettings placementsettings = this.createPlacementSettings(rotationIn, boundsIn);
//        return template.addBlocksToWorld(worldIn, pos, placementsettings, 18);
//    }
//
//    @Override
//    public INBT serialize0() {
//        return NBTHelper.createMap(ImmutableMap.of(StringNBT.valueOf("Loc"),
//                                                   StringNBT.valueOf(this.location.toString()),
//                                                   StringNBT.valueOf("Proc"),
//                                                   NBTHelper.createList(this.processors.stream()
//                                                                                       .map(processor -> processor.process(NBTDynamicOps.INSTANCE)
//                                                                                                                  .getValue()))));
//    }
//
//    @Override
//    public String toString() {
//        return "SinglePuzzlePiece[" + this.location + "]";
//    }
//}
