package tgw.evolution.init;

import net.minecraft.tileentity.TileEntityType;
import net.minecraft.tileentity.TileEntityType.Builder;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.tileentities.*;

import static tgw.evolution.init.EvolutionBlocks.*;

public class EvolutionTileEntities {

    public static final DeferredRegister<TileEntityType<?>> TILES = new DeferredRegister<>(ForgeRegistries.TILE_ENTITIES, Evolution.MODID);

    public static final RegistryObject<TileEntityType<?>> TE_FECES = TILES.register("te_feces",
                                                                                    () -> Builder.create(TEFeces::new, FECES.get()).build(null));
    public static final RegistryObject<TileEntityType<?>> TE_KNAPPING = TILES.register("te_knapping",
                                                                                       () -> Builder.create(TEKnapping::new,
                                                                                                            KNAPPING_ANDESITE.get(),
                                                                                                            KNAPPING_BASALT.get(),
                                                                                                            KNAPPING_CHALK.get(),
                                                                                                            KNAPPING_CHERT.get(),
                                                                                                            KNAPPING_CONGLOMERATE.get(),
                                                                                                            KNAPPING_DACITE.get(),
                                                                                                            KNAPPING_DOLOMITE.get(),
                                                                                                            KNAPPING_DOLOMITE.get(),
                                                                                                            KNAPPING_GABBRO.get(),
                                                                                                            KNAPPING_GNEISS.get(),
                                                                                                            KNAPPING_GRANITE.get(),
                                                                                                            KNAPPING_LIMESTONE.get(),
                                                                                                            KNAPPING_MARBLE.get(),
                                                                                                            KNAPPING_PHYLLITE.get(),
                                                                                                            KNAPPING_QUARTZITE.get(),
                                                                                                            KNAPPING_RED_SANDSTONE.get(),
                                                                                                            KNAPPING_SANDSTONE.get(),
                                                                                                            KNAPPING_SCHIST.get(),
                                                                                                            KNAPPING_SHALE.get(),
                                                                                                            KNAPPING_SLATE.get()).build(null));
    public static final RegistryObject<TileEntityType<?>> TE_MOLDING = TILES.register("te_molding",
                                                                                      () -> Builder.create(TEMolding::new, MOLDING.get())
                                                                                                   .build(null));
    //    public static final RegistryObject<TileEntityType<?>> TE_SHADOWHOUND = TILES.register("te_shadowhound", () -> Builder.create(TEShadowHound::new, SHADOWHOUND.get()).build(null));
    public static final RegistryObject<TileEntityType<?>> TE_TORCH = TILES.register("te_torch",
                                                                                    () -> Builder.create(TETorch::new, WALL_TORCH.get(), TORCH.get())
                                                                                                 .build(null));
    public static final RegistryObject<TileEntityType<?>> TE_METAL = TILES.register("te_metal",
                                                                                    () -> Builder.create(TEMetal::new, BLOCK_COPPER.get())
                                                                                                 .build(null));
    public static final RegistryObject<TileEntityType<?>> TE_CHOPPING = TILES.register("te_chopping",
                                                                                       () -> Builder.create(TEChopping::new,
                                                                                                            CHOPPING_BLOCK_ACACIA.get(),
                                                                                                            CHOPPING_BLOCK_ASPEN.get(),
                                                                                                            CHOPPING_BLOCK_BIRCH.get(),
                                                                                                            CHOPPING_BLOCK_CEDAR.get(),
                                                                                                            CHOPPING_BLOCK_ELM.get(),
                                                                                                            CHOPPING_BLOCK_EBONY.get(),
                                                                                                            CHOPPING_BLOCK_EUCALYPTUS.get(),
                                                                                                            CHOPPING_BLOCK_FIR.get(),
                                                                                                            CHOPPING_BLOCK_KAPOK.get(),
                                                                                                            CHOPPING_BLOCK_MANGROVE.get(),
                                                                                                            CHOPPING_BLOCK_MAPLE.get(),
                                                                                                            CHOPPING_BLOCK_OAK.get(),
                                                                                                            CHOPPING_BLOCK_OLD_OAK.get(),
                                                                                                            CHOPPING_BLOCK_PALM.get(),
                                                                                                            CHOPPING_BLOCK_PINE.get(),
                                                                                                            CHOPPING_BLOCK_REDWOOD.get(),
                                                                                                            CHOPPING_BLOCK_SPRUCE.get(),
                                                                                                            CHOPPING_BLOCK_WILLOW.get()).build(null));
    public static final RegistryObject<TileEntityType<?>> TE_PIT_KILN = TILES.register("te_pit_kiln",
                                                                                       () -> Builder.create(TEPitKiln::new, PIT_KILN.get())
                                                                                                    .build(null));
    public static final RegistryObject<TileEntityType<?>> TE_PUZZLE = TILES.register("te_puzzle",
                                                                                     () -> Builder.create(TEPuzzle::new, PUZZLE.get()).build(null));
    public static final RegistryObject<TileEntityType<?>> TE_SCHEMATIC = TILES.register("te_schematic",
                                                                                        () -> Builder.create(TESchematic::new, SCHEMATIC_BLOCK.get())
                                                                                                     .build(null));

    public static void register() {
        TILES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
