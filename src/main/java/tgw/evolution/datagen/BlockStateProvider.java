//package tgw.evolution.datagen;
//
//import com.google.common.base.Preconditions;
//import com.google.common.collect.ImmutableList;
//import com.google.gson.JsonArray;
//import com.google.gson.JsonElement;
//import com.google.gson.JsonObject;
//import it.unimi.dsi.fastutil.booleans.Boolean2BooleanFunction;
//import it.unimi.dsi.fastutil.booleans.Boolean2ObjectFunction;
//import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
//import it.unimi.dsi.fastutil.objects.Reference2ObjectLinkedOpenHashMap;
//import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
//import net.minecraft.core.Direction;
//import net.minecraft.core.Registry;
//import net.minecraft.data.DataGenerator;
//import net.minecraft.data.HashCache;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.util.StringRepresentable;
//import net.minecraft.world.item.Items;
//import net.minecraft.world.level.block.Block;
//import net.minecraft.world.level.block.RotatedPillarBlock;
//import net.minecraft.world.level.block.state.BlockState;
//import net.minecraft.world.level.block.state.properties.BooleanProperty;
//import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
//import net.minecraft.world.level.block.state.properties.EnumProperty;
//import net.minecraftforge.client.model.generators.BlockModelBuilder;
//import net.minecraftforge.client.model.generators.IGeneratedBlockstate;
//import net.minecraftforge.client.model.generators.ItemModelBuilder;
//import net.minecraftforge.client.model.generators.ModelFile;
//import tgw.evolution.Evolution;
//import tgw.evolution.blocks.BlockLog;
//import tgw.evolution.blocks.BlockXYZAxis;
//import tgw.evolution.blocks.tileentities.SchematicMode;
//import tgw.evolution.blocks.util.IntProperty;
//import tgw.evolution.datagen.blockstates.ConfiguredModel;
//import tgw.evolution.datagen.blockstates.MultiPartBlockStateBuilder;
//import tgw.evolution.datagen.blockstates.VariantBlockStateBuilder;
//import tgw.evolution.init.EvolutionBStates;
//import tgw.evolution.init.EvolutionBlocks;
//import tgw.evolution.util.constants.RockVariant;
//import tgw.evolution.util.constants.WoodVariant;
//
//import java.io.IOException;
//import java.nio.file.Path;
//import java.util.Arrays;
//import java.util.Collection;
//import java.util.List;
//import java.util.function.Function;
//
//public class BlockStateProvider implements EvolutionDataProvider<ResourceLocation> {
//
//    protected final DataGenerator generator;
//    protected final Reference2ObjectMap<Block, IGeneratedBlockstate> registeredBlocks = new Reference2ObjectLinkedOpenHashMap<>();
//    private final BlockModelProvider blockModels;
//    private final Collection<Path> existingPaths;
//    private final ItemModelProvider itemModels;
//
//    public BlockStateProvider(GenBundle bundle) {
//        this(bundle, Evolution.MODID);
//    }
//
//    public BlockStateProvider(GenBundle bundle, String modId) {
//        this.generator = bundle.generator();
//        this.blockModels = new BlockModelProvider(bundle, modId, ModelProvider.BLOCK_FOLDER, BlockModelBuilder::new) {
//            @Override
//            protected void registerModels() {
//            }
//        };
//        this.itemModels = new ItemModelProvider(bundle, modId, ModelProvider.ITEM_FOLDER, ItemModelBuilder::new) {
//            @Override
//            protected void registerModels() {
//            }
//        };
//        this.existingPaths = bundle.existingPaths();
//    }
//
//    public static ResourceLocation blockTexture(Block block, String post) {
//        ResourceLocation name = block.getRegistryName();
//        assert name != null;
//        return new ResourceLocation(name.getNamespace(), "block/" + name.getPath() + post);
//    }
//
//    public static ResourceLocation blockTexture(Block block) {
//        ResourceLocation name = block.getRegistryName();
//        assert name != null;
//        return new ResourceLocation(name.getNamespace(), "block/" + name.getPath());
//    }
//
//    private static String name(Block block, String post) {
//        return name(block) + post;
//    }
//
//    private static String name(Block block) {
//        //noinspection ConstantConditions
//        return block.getRegistryName().getPath();
//    }
//
//    public void axisBlock(BlockXYZAxis block, ResourceLocation side, ResourceLocation end) {
//        this.axisBlock(block, this.models().cubeColumn(name(block), side, end));
//    }
//
//    public void axisBlock(BlockXYZAxis block, ModelFile model) {
//        if (Registry.ITEM.get(block.getRegistryName()) != Items.AIR) {
//            this.itemModels().simpleBlock(block);
//        }
//        this.getVariantBuilder(block)
//            .partialState()
//            .with(EvolutionBStates.AXIS, Direction.Axis.Y)
//            .modelForState()
//            .modelFile(model)
//            .addModel()
//            .partialState()
//            .with(RotatedPillarBlock.AXIS, Direction.Axis.Z)
//            .modelForState()
//            .modelFile(model)
//            .rotationX(90)
//            .addModel()
//            .partialState()
//            .with(RotatedPillarBlock.AXIS, Direction.Axis.X)
//            .modelForState()
//            .modelFile(model)
//            .rotationX(90)
//            .rotationY(90)
//            .addModel();
//    }
//
//    public void blockBooleanProperty(Block block, BooleanProperty property, Boolean2ObjectFunction<ModelFile> modelMaker) {
//        if (Registry.ITEM.get(block.getRegistryName()) != Items.AIR) {
//            this.itemModels().simpleBlock(block);
//        }
//        this.getVariantBuilder(block)
//            .partialState()
//            .with(property, true)
//            .setModels(new ConfiguredModel(modelMaker.apply(true)));
//        this.getVariantBuilder(block)
//            .partialState()
//            .with(property, false)
//            .setModels(new ConfiguredModel(modelMaker.apply(false)));
//
//    }
//
//    public void blockBooleanPropertyRandomRotation(Block block,
//                                                   BooleanProperty property,
//                                                   Boolean2BooleanFunction shouldApplyRotation,
//                                                   Boolean2ObjectFunction<ModelFile> modelMaker) {
//        if (Registry.ITEM.get(block.getRegistryName()) != Items.AIR) {
//            this.itemModels().simpleBlock(block);
//        }
//        ModelFile trueModel = modelMaker.get(true);
//        ModelFile falseModel = modelMaker.get(false);
//        VariantBlockStateBuilder.PartialBlockstate builder = this.getVariantBuilder(block)
//                                                                 .partialState()
//                                                                 .with(property, true);
//        if (shouldApplyRotation.get(true)) {
//            builder.setModels(new ConfiguredModel(trueModel),
//                              new ConfiguredModel(trueModel, 0, 90, false),
//                              new ConfiguredModel(trueModel, 0, 180, false),
//                              new ConfiguredModel(trueModel, 0, 270, false));
//        }
//        else {
//            builder.setModels(new ConfiguredModel(trueModel));
//        }
//        builder = this.getVariantBuilder(block)
//                      .partialState()
//                      .with(property, false);
//        if (shouldApplyRotation.get(false)) {
//            builder.setModels(new ConfiguredModel(falseModel),
//                              new ConfiguredModel(falseModel, 0, 90, false),
//                              new ConfiguredModel(falseModel, 0, 180, false),
//                              new ConfiguredModel(falseModel, 0, 270, false));
//        }
//        else {
//            builder.setModels(new ConfiguredModel(falseModel));
//        }
//    }
//
//    public <T extends Enum<T> & StringRepresentable> void blockEnumProperty(Block block,
//                                                                            EnumProperty<T> property,
//                                                                            T[] values,
//                                                                            Function<T, ModelFile> modelMaker) {
//        for (T value : values) {
//            //noinspection ObjectAllocationInLoop
//            this.getVariantBuilder(block).partialState().with(property, value).setModels(new ConfiguredModel(modelMaker.apply(value)));
//        }
//    }
//
//    public void blockIntegerProperty(Block block, IntProperty property, Int2ObjectFunction<ModelFile> modelMaker) {
//        for (int i = property.getMinValue(); i <= property.getMaxValue(); i++) {
//            //noinspection ObjectAllocationInLoop
//            this.getVariantBuilder(block).partialState().with(property, i).setModels(new ConfiguredModel(modelMaker.get(i)));
//        }
//    }
//
//    public ModelFile cubeAll(Block block) {
//        return this.models().cubeAll(name(block), blockTexture(block));
//    }
//
//    public void directionalBlock(Block block, ModelFile model) {
//        this.directionalBlock(block, model, 180);
//    }
//
//    public void directionalBlock(Block block, ModelFile model, int angleOffset) {
//        this.directionalBlock(block, state -> model, angleOffset);
//    }
//
//    public void directionalBlock(Block block, Function<BlockState, ModelFile> modelFunc) {
//        this.directionalBlock(block, modelFunc, 180);
//    }
//
//    public void directionalBlock(Block block, Function<BlockState, ModelFile> modelFunc, int angleOffset) {
//        if (Registry.ITEM.get(block.getRegistryName()) != Items.AIR) {
//            this.itemModels().simpleBlock(block);
//        }
//        this.getVariantBuilder(block).forAllStates(state -> {
//            Direction dir = state.getValue(EvolutionBStates.DIRECTION);
//            return ConfiguredModel.builder()
//                                  .modelFile(modelFunc.apply(state))
//                                  .rotationX(dir == Direction.DOWN ? 180 : dir.getAxis().isHorizontal() ? 90 : 0)
//                                  .rotationY(dir.getAxis().isVertical() ? 0 : ((int) dir.toYRot() + angleOffset) % 360)
//                                  .build();
//        });
//    }
//
//    public ModelFile existingModel(String name) {
//        return this.models().getExistingFile(new ResourceLocation(name));
//    }
//
//    @Override
//    public Collection<Path> existingPaths() {
//        return this.existingPaths;
//    }
//
//    public MultiPartBlockStateBuilder getMultipartBuilder(Block b) {
//        if (this.registeredBlocks.containsKey(b)) {
//            IGeneratedBlockstate old = this.registeredBlocks.get(b);
//            Preconditions.checkState(old instanceof MultiPartBlockStateBuilder);
//            return (MultiPartBlockStateBuilder) old;
//        }
//        MultiPartBlockStateBuilder ret = new MultiPartBlockStateBuilder(b);
//        this.registeredBlocks.put(b, ret);
//        return ret;
//    }
//
//    @Override
//    public String getName() {
//        return "Evolution BlockStates";
//    }
//
//    public VariantBlockStateBuilder getVariantBuilder(Block b) {
//        if (this.registeredBlocks.containsKey(b)) {
//            IGeneratedBlockstate old = this.registeredBlocks.get(b);
//            Preconditions.checkState(old instanceof VariantBlockStateBuilder);
//            return (VariantBlockStateBuilder) old;
//        }
//        VariantBlockStateBuilder ret = new VariantBlockStateBuilder(b);
//        this.registeredBlocks.put(b, ret);
//        return ret;
//    }
//
//    public ItemModelProvider itemModels() {
//        return this.itemModels;
//    }
//
//    @Override
//    public String makePath(ResourceLocation id) {
//        return "assets/" + id.getNamespace() + "/blockstates/" + id.getPath() + ".json";
//    }
//
//    public BlockModelProvider models() {
//        return this.blockModels;
//    }
//
//    @SuppressWarnings("ObjectAllocationInLoop")
//    protected void registerStatesAndModels() {
//        //Temporary
//        this.simpleBlock(EvolutionBlocks.GLASS.get());
//        this.simpleBlock(EvolutionBlocks.PLACEHOLDER_BLOCK.get());
//        //Dev
//        this.blockIntegerProperty(EvolutionBlocks.ATM.get(), EvolutionBStates.ATM,
//                                  atm -> this.models()
//                                             .cross(name(EvolutionBlocks.ATM.get(), "_" + atm),
//                                                    blockTexture(EvolutionBlocks.ATM.get(), "_" + atm)));
//        this.simpleBlock(EvolutionBlocks.DESTROY_3.get());
//        this.simpleBlock(EvolutionBlocks.DESTROY_6.get());
//        this.simpleBlock(EvolutionBlocks.DESTROY_9.get());
//        this.directionalBlock(EvolutionBlocks.PUZZLE.get(), this.models()
//                                                                .cubeBottomTop(name(EvolutionBlocks.PUZZLE.get()),
//                                                                               blockTexture(EvolutionBlocks.PUZZLE.get(), "_side"),
//                                                                               blockTexture(EvolutionBlocks.PUZZLE.get(), "_bottom"),
//                                                                               blockTexture(EvolutionBlocks.PUZZLE.get(), "_top")));
//        this.blockEnumProperty(EvolutionBlocks.SCHEMATIC_BLOCK.get(), EvolutionBStates.SCHEMATIC_MODE, SchematicMode.values(),
//                               mode -> this.models()
//                                           .cubeAll(name(EvolutionBlocks.SCHEMATIC_BLOCK.get(), "_" + mode.getSerializedName()),
//                                                    blockTexture(EvolutionBlocks.SCHEMATIC_BLOCK.get(), "_" + mode.getSerializedName())));
//        this.itemModels().simpleBlock(EvolutionBlocks.SCHEMATIC_BLOCK.get(), "_corner");
//        //Independent
//        Block clay = EvolutionBlocks.CLAY.get();
//        this.simpleBlock(clay);
//        this.getMultipartBuilder(EvolutionBlocks.FIRE.get())
//            .part()
//            .modelFile(this.existingModel("block/fire_floor0"))
//            .nextModel()
//            .modelFile(this.existingModel("block/fire_floor1"))
//            .addModel()
//            .condition(EvolutionBStates.NORTH, false)
//            .condition(EvolutionBStates.EAST, false)
//            .condition(EvolutionBStates.SOUTH, false)
//            .condition(EvolutionBStates.WEST, false)
//            .condition(EvolutionBStates.UP, false)
//            .end()
//            .part()
//            .modelFile(this.existingModel("block/fire_side0"))
//            .nextModel()
//            .modelFile(this.existingModel("block/fire_side1"))
//            .nextModel()
//            .modelFile(this.existingModel("block/fire_side_alt0"))
//            .nextModel()
//            .modelFile(this.existingModel("block/fire_side_alt1"))
//            .addModel()
//            .useOr()
//            .nestedGroup()
//            .condition(EvolutionBStates.NORTH, true)
//            .end()
//            .nestedGroup()
//            .condition(EvolutionBStates.NORTH, false)
//            .condition(EvolutionBStates.EAST, false)
//            .condition(EvolutionBStates.SOUTH, false)
//            .condition(EvolutionBStates.WEST, false)
//            .condition(EvolutionBStates.UP, false)
//            .end()
//            .end()
//            .part()
//            .modelFile(this.existingModel("block/fire_side0"))
//            .rotationY(90)
//            .nextModel()
//            .modelFile(this.existingModel("block/fire_side1"))
//            .rotationY(90)
//            .nextModel()
//            .modelFile(this.existingModel("block/fire_side_alt0"))
//            .rotationY(90)
//            .nextModel()
//            .modelFile(this.existingModel("block/fire_side_alt1"))
//            .rotationY(90)
//            .addModel()
//            .useOr()
//            .nestedGroup()
//            .condition(EvolutionBStates.EAST, true)
//            .end()
//            .nestedGroup()
//            .condition(EvolutionBStates.NORTH, false)
//            .condition(EvolutionBStates.EAST, false)
//            .condition(EvolutionBStates.SOUTH, false)
//            .condition(EvolutionBStates.WEST, false)
//            .condition(EvolutionBStates.UP, false)
//            .end()
//            .end()
//            .part()
//            .modelFile(this.existingModel("block/fire_side0"))
//            .rotationY(180)
//            .nextModel()
//            .modelFile(this.existingModel("block/fire_side1"))
//            .rotationY(180)
//            .nextModel()
//            .modelFile(this.existingModel("block/fire_side_alt0"))
//            .rotationY(180)
//            .nextModel()
//            .modelFile(this.existingModel("block/fire_side_alt1"))
//            .rotationY(180)
//            .addModel()
//            .useOr()
//            .nestedGroup()
//            .condition(EvolutionBStates.SOUTH, true)
//            .end()
//            .nestedGroup()
//            .condition(EvolutionBStates.NORTH, false)
//            .condition(EvolutionBStates.EAST, false)
//            .condition(EvolutionBStates.SOUTH, false)
//            .condition(EvolutionBStates.WEST, false)
//            .condition(EvolutionBStates.UP, false)
//            .end()
//            .end()
//            .part()
//            .modelFile(this.existingModel("block/fire_side0"))
//            .rotationY(270)
//            .nextModel()
//            .modelFile(this.existingModel("block/fire_side1"))
//            .rotationY(270)
//            .nextModel()
//            .modelFile(this.existingModel("block/fire_side_alt0"))
//            .rotationY(270)
//            .nextModel()
//            .modelFile(this.existingModel("block/fire_side_alt1"))
//            .rotationY(270)
//            .addModel()
//            .useOr()
//            .nestedGroup()
//            .condition(EvolutionBStates.WEST, true)
//            .end()
//            .nestedGroup()
//            .condition(EvolutionBStates.NORTH, false)
//            .condition(EvolutionBStates.EAST, false)
//            .condition(EvolutionBStates.SOUTH, false)
//            .condition(EvolutionBStates.WEST, false)
//            .condition(EvolutionBStates.UP, false)
//            .end()
//            .end()
//            .part()
//            .modelFile(this.existingModel("block/fire_up0"))
//            .nextModel()
//            .modelFile(this.existingModel("block/fire_up1"))
//            .nextModel()
//            .modelFile(this.existingModel("block/fire_up_alt0"))
//            .nextModel()
//            .modelFile(this.existingModel("block/fire_up_alt1"))
//            .addModel()
//            .condition(EvolutionBStates.UP, true)
//            .end();
//        Block firewoodPile = EvolutionBlocks.FIREWOOD_PILE.get();
//        this.simpleBlockNoItem(firewoodPile, this.models().builtin(name(firewoodPile), blockTexture(WoodVariant.OAK.get(EvolutionBlocks.LOGS))));
//        Block peat = EvolutionBlocks.PEAT.get();
//        this.blockIntegerProperty(peat, EvolutionBStates.LAYERS_1_4, layers -> switch (layers) {
//            case 1 -> this.models()
//                          .withExistingParent(name(peat, "_1"), "evolution:slab_4_1")
//                          .texture("side", blockTexture(peat))
//                          .texture("top", blockTexture(peat))
//                          .texture("bottom", blockTexture(peat));
//            case 2 -> this.models().slab(name(peat, "_2"), blockTexture(peat), blockTexture(peat), blockTexture(peat));
//            case 3 -> this.models()
//                          .withExistingParent(name(peat, "_3"), "evolution:slab_4_3")
//                          .texture("side", blockTexture(peat))
//                          .texture("top", blockTexture(peat))
//                          .texture("bottom", blockTexture(peat));
//            case 4 -> this.models().cubeAll(name(peat, "_4"), blockTexture(peat));
//            default -> throw new IllegalStateException("Invalid layers: " + layers);
//        });
//        this.itemModels().simpleBlock(peat, "_1");
//        //Collections
//        Block clayGrass = RockVariant.CLAY.get(EvolutionBlocks.GRASSES);
//        this.blockBooleanPropertyRandomRotation(clayGrass, EvolutionBStates.SNOWY, snowy -> !snowy, snowy -> {
//            if (!snowy) {
//                return this.models().withExistingParent(name(clayGrass), "evolution:block/grass")
//                           .texture("side", blockTexture(clay))
//                           .texture("overlay", "evolution:block/grass_side_overlay")
//                           .texture("bottom", blockTexture(clay))
//                           .texture("top", "evolution:block/grass_top")
//                           .texture("particle", blockTexture(clay));
//            }
//            return this.models().withExistingParent(name(clayGrass, "_snow"), "evolution:block/grass_snow")
//                       .texture("side", blockTexture(clay))
//                       .texture("overlay", "evolution:block/grass_side_overlay_snow")
//                       .texture("bottom", blockTexture(clay))
//                       .texture("top", "evolution:block/grass_top")
//                       .texture("particle", blockTexture(clay));
//        });
//        Block peatGrass = RockVariant.PEAT.get(EvolutionBlocks.GRASSES);
//        this.blockBooleanPropertyRandomRotation(peatGrass, EvolutionBStates.SNOWY, snowy -> !snowy, snowy -> {
//            if (!snowy) {
//                return this.models().withExistingParent(name(peatGrass), "evolution:block/grass")
//                           .texture("side", blockTexture(peat))
//                           .texture("overlay", "evolution:block/grass_side_overlay")
//                           .texture("bottom", blockTexture(peat))
//                           .texture("top", "evolution:block/grass_top")
//                           .texture("particle", blockTexture(peat));
//            }
//            return this.models().withExistingParent(name(peatGrass, "_snow"), "evolution:block/grass_snow")
//                       .texture("side", blockTexture(peat))
//                       .texture("overlay", "evolution:block/grass_side_overlay_snow")
//                       .texture("bottom", blockTexture(peat))
//                       .texture("top", "evolution:block/grass_top")
//                       .texture("particle", blockTexture(peat));
//        });
//        Block tallgrass = EvolutionBlocks.TALLGRASS.get();
//        this.simpleBlockNoItem(tallgrass,
//                               this.models().withExistingParent(name(tallgrass), "block/tinted_cross").texture("cross", blockTexture(tallgrass)));
//        Block tallgrassHigh = EvolutionBlocks.TALLGRASS_HIGH.get();
//        this.blockEnumProperty(tallgrassHigh, EvolutionBStates.HALF, DoubleBlockHalf.values(), half ->
//                this.models()
//                    .withExistingParent(name(tallgrassHigh, half == DoubleBlockHalf.UPPER ? "_top" : "_bottom"), "block/tinted_cross")
//                    .texture("cross", blockTexture(tallgrassHigh, half == DoubleBlockHalf.UPPER ? "_top" : "_bottom")));
//        for (RockVariant variant : RockVariant.VALUES_STONE) {
//            Block cobblestone = variant.get(EvolutionBlocks.COBBLESTONES);
//            this.simpleBlock(cobblestone);
//            Block dirt = variant.get(EvolutionBlocks.DIRTS);
//            this.simpleBlockWithRandomRotation(dirt);
//            Block dryGrass = variant.get(EvolutionBlocks.DRY_GRASSES);
//            this.blockBooleanPropertyRandomRotation(dryGrass, EvolutionBStates.SNOWY, snowy -> !snowy, snowy -> {
//                if (!snowy) {
//                    return this.models().withExistingParent(name(dryGrass), "evolution:block/dry_grass")
//                               .texture("side", blockTexture(dirt))
//                               .texture("upover", "evolution:block/dry_grass_top")
//                               .texture("overlay", "evolution:block/dry_grass_side_overlay")
//                               .texture("bottom", blockTexture(dirt))
//                               .texture("top", blockTexture(dirt))
//                               .texture("particle", blockTexture(dirt));
//                }
//                return this.models().withExistingParent(name(dryGrass, "_snow"), "evolution:block/dry_grass_snow")
//                           .texture("side", blockTexture(dirt))
//                           .texture("top", "evolution:block/dry_grass_top")
//                           .texture("overlay", "evolution:block/dry_grass_side_overlay_snow")
//                           .texture("bottom", blockTexture(dirt))
//                           .texture("particle", blockTexture(dirt));
//            });
//            Block grass = variant.get(EvolutionBlocks.GRASSES);
//            this.blockBooleanPropertyRandomRotation(grass, EvolutionBStates.SNOWY, snowy -> !snowy, snowy -> {
//                if (!snowy) {
//                    return this.models().withExistingParent(name(grass), "evolution:block/grass")
//                               .texture("side", blockTexture(dirt))
//                               .texture("overlay", "evolution:block/grass_side_overlay")
//                               .texture("bottom", blockTexture(dirt))
//                               .texture("top", "evolution:block/grass_top")
//                               .texture("particle", blockTexture(dirt));
//                }
//                return this.models().withExistingParent(name(grass, "_snow"), "evolution:block/grass_snow")
//                           .texture("side", blockTexture(dirt))
//                           .texture("overlay", "evolution:block/grass_side_overlay_snow")
//                           .texture("bottom", blockTexture(dirt))
//                           .texture("top", "evolution:block/grass_top")
//                           .texture("particle", blockTexture(dirt));
//            });
//            this.simpleBlock(variant.get(EvolutionBlocks.GRAVELS));
//            Block knappingBlock = variant.get(EvolutionBlocks.KNAPPING_BLOCKS);
//            this.simpleBlock(knappingBlock, this.models().builtin(name(knappingBlock), blockTexture(cobblestone)));
//            this.simpleBlock(variant.get(EvolutionBlocks.POLISHED_STONES));
//            Block rock = variant.get(EvolutionBlocks.ROCKS);
//            this.simpleBlockWithRandomRotation(rock, this.models()
//                                                         .withExistingParent(name(rock), "evolution:block/rock")
//                                                         .texture("0", blockTexture(cobblestone))
//                                                         .texture("particle", blockTexture(cobblestone)));
//            this.simpleBlockWithRandomRotation(variant.get(EvolutionBlocks.SANDS));
//            this.simpleBlock(variant.get(EvolutionBlocks.STONEBRICKS));
//            this.simpleBlock(variant.get(EvolutionBlocks.STONES));
//        }
//        for (WoodVariant variant : WoodVariant.VALUES) {
//            Block choppingBlock = variant.get(EvolutionBlocks.CHOPPING_BLOCKS);
//            BlockLog log = variant.get(EvolutionBlocks.LOGS);
//            this.simpleBlock(choppingBlock,
//                             this.models().slab(name(choppingBlock), blockTexture(log), blockTexture(log, "_top"), blockTexture(log, "_top")));
//            Block leaves = variant.get(EvolutionBlocks.LEAVES);
//            this.simpleBlock(leaves, this.models().leaves(name(leaves), blockTexture(leaves)));
//            this.axisBlock(log, blockTexture(log), blockTexture(log, "_top"));
//            this.simpleBlock(variant.get(EvolutionBlocks.PLANKS));
//            Block sapling = variant.get(EvolutionBlocks.SAPLINGS);
//            this.simpleBlockNoItem(sapling, this.models().cross(name(sapling), blockTexture(sapling)));
//        }
//    }
//
//    @Override
//    public void run(HashCache cache) throws IOException {
//        this.models().clear();
//        this.itemModels().clear();
//        this.registeredBlocks.clear();
//        this.registerStatesAndModels();
//        this.models().generateAll(cache);
//        this.itemModels().generateAll(cache);
//        for (Reference2ObjectMap.Entry<Block, IGeneratedBlockstate> entry : this.registeredBlocks.reference2ObjectEntrySet()) {
//            this.saveBlockState(cache, entry.getValue().toJson(), entry.getKey());
//        }
//    }
//
//    private void saveBlockState(HashCache cache, JsonObject stateJson, Block owner) {
//        ResourceLocation blockName = Preconditions.checkNotNull(owner.getRegistryName());
//        Path path = this.generator.getOutputFolder().resolve(this.makePath(blockName));
//        this.save(cache, stateJson, path, blockName);
//    }
//
//    public void simpleBlock(Block block, ModelFile model) {
//        this.simpleBlock(block, new ConfiguredModel(model));
//    }
//
//    public void simpleBlock(Block block, ConfiguredModel... models) {
//        if (Registry.ITEM.get(block.getRegistryName()) != Items.AIR) {
//            this.itemModels().simpleBlock(block);
//        }
//        this.getVariantBuilder(block).partialState().setModels(models);
//    }
//
//    public void simpleBlock(Block block) {
//        this.simpleBlock(block, this.cubeAll(block));
//    }
//
//    public void simpleBlockNoItem(Block block, ModelFile model) {
//        this.simpleBlockNoItem(block, new ConfiguredModel(model));
//    }
//
//    public void simpleBlockNoItem(Block block, ConfiguredModel... models) {
//        this.getVariantBuilder(block).partialState().setModels(models);
//    }
//
//    public void simpleBlockWithRandomRotation(Block block) {
//        this.simpleBlockWithRandomRotation(block, this.cubeAll(block));
//    }
//
//    public void simpleBlockWithRandomRotation(Block block, ModelFile model) {
//        this.simpleBlock(block, new ConfiguredModel(model),
//                         new ConfiguredModel(model, 0, 90, false),
//                         new ConfiguredModel(model, 0, 180, false),
//                         new ConfiguredModel(model, 0, 270, false));
//    }
//
//    @Override
//    public String type() {
//        return "BlockState";
//    }
//
//    public static class ConfiguredModelList {
//
//        private final List<ConfiguredModel> models;
//
//        private ConfiguredModelList(List<ConfiguredModel> models) {
//            Preconditions.checkArgument(!models.isEmpty());
//            this.models = models;
//        }
//
//        public ConfiguredModelList(ConfiguredModel model) {
//            this(ImmutableList.of(model));
//        }
//
//        public ConfiguredModelList(ConfiguredModel... models) {
//            this(Arrays.asList(models));
//        }
//
//        public BlockStateProvider.ConfiguredModelList append(ConfiguredModel... models) {
//            return new BlockStateProvider.ConfiguredModelList(ImmutableList.<ConfiguredModel>builder().addAll(this.models).add(models).build());
//        }
//
//        public JsonElement toJSON() {
//            if (this.models.size() == 1) {
//                return this.models.get(0).toJSON(false);
//            }
//            JsonArray ret = new JsonArray();
//            for (ConfiguredModel m : this.models) {
//                ret.add(m.toJSON(true));
//            }
//            return ret;
//        }
//    }
//}
