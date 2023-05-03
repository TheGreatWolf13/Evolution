//package tgw.evolution.world.puzzle.pieces;
//
//import com.google.common.collect.ImmutableMap;
//import com.google.common.collect.Lists;
//import com.mojang.datafixers.Dynamic;
//import net.minecraft.nbt.CompoundNBT;
//import net.minecraft.nbt.INBT;
//import net.minecraft.nbt.NBTDynamicOps;
//import net.minecraft.nbt.StringNBT;
//import net.minecraft.util.Direction;
//import net.minecraft.util.Rotation;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.util.math.MutableBoundingBox;
//import net.minecraft.world.IWorld;
//import net.minecraft.world.gen.ChunkGenerator;
//import net.minecraft.world.gen.feature.ConfiguredFeature;
//import net.minecraft.world.gen.feature.template.Template;
//import net.minecraft.world.gen.feature.template.TemplateManager;
//import net.minecraftforge.registries.ForgeRegistries;
//import tgw.evolution.blocks.BlockPuzzle;
//import tgw.evolution.init.EvolutionBlocks;
//import tgw.evolution.util.constants.NBTHelper;
//import tgw.evolution.world.puzzle.EnumPuzzleType;
//import tgw.evolution.world.puzzle.PuzzlePiece;
//import tgw.evolution.world.puzzle.pieces.config.PlacementType;
//
//import java.util.List;
//import java.util.Random;
//
//public class FeaturePuzzlePiece extends PuzzlePiece {
//
//    private final ConfiguredFeature<?, ?> feature;
//    private final CompoundNBT nbt;
//
//    public FeaturePuzzlePiece(ConfiguredFeature<?, ?> feature) {
//        this(feature, PlacementType.RIGID);
//    }
//
//    public FeaturePuzzlePiece(ConfiguredFeature<?, ?> feature, PlacementType placementBehaviour) {
//        super(placementBehaviour);
//        this.feature = feature;
//        this.nbt = this.writeNBT();
//    }
//
//    public FeaturePuzzlePiece(INBT nbt) {
//        super(nbt);
//        this.feature = ConfiguredFeature.deserialize(new Dynamic<>(NBTDynamicOps.INSTANCE, nbt).get("Feature").orElseEmptyMap());
//        this.nbt = this.writeNBT();
//    }
//
//    public BlockPos func_214868_a(TemplateManager manager, Rotation rotation) {
//        return BlockPos.ZERO;
//    }
//
//    @Override
//    public MutableBoundingBox getBoundingBox(TemplateManager manager, BlockPos pos, Rotation rotation) {
//        BlockPos blockpos = this.func_214868_a(manager, rotation);
//        return new MutableBoundingBox(pos.getX(),
//                                      pos.getY(),
//                                      pos.getZ(),
//                                      pos.getX() + blockpos.getX(),
//                                      pos.getY() + blockpos.getY(),
//                                      pos.getZ() + blockpos.getZ());
//    }
//
//    @Override
//    public List<Template.BlockInfo> getPuzzleBlocks(TemplateManager templateManagerIn, BlockPos pos, Rotation rotationIn, Random rand) {
//        List<Template.BlockInfo> list = Lists.newArrayList();
//        list.add(new Template.BlockInfo(pos, EvolutionBlocks.PUZZLE.get().getDefaultState().with(BlockPuzzle.FACING, Direction.DOWN), this.nbt));
//        return list;
//    }
//
//    @Override
//    public EnumPuzzleType getType() {
//        return EnumPuzzleType.FEATURE;
//    }
//
//    @Override
//    public boolean place(TemplateManager manager, IWorld world, BlockPos pos, Rotation rotation, MutableBoundingBox boundingBox, Random rand) {
//        ChunkGenerator<?> chunkGenerator = world.getChunkProvider().getChunkGenerator();
//        return this.feature.place(world, chunkGenerator, rand, pos);
//    }
//
//    @Override
//    public INBT serialize0() {
//        return NBTHelper.createMap(ImmutableMap.of(new StringNBT("Feature"), this.feature.serialize(NBTDynamicOps.INSTANCE).getValue()));
//    }
//
//    @Override
//    public String toString() {
//        return "Feature[" + ForgeRegistries.FEATURES.getKey(this.feature.feature) + "]";
//    }
//
//    public CompoundNBT writeNBT() {
//        CompoundNBT compoundnbt = new CompoundNBT();
//        compoundnbt.putString("TargetPool", "minecraft:empty");
//        compoundnbt.putString("AttachmentType", "minecraft:bottom");
//        compoundnbt.putString("FinalState", "minecraft:air");
//        return compoundnbt;
//    }
//}
