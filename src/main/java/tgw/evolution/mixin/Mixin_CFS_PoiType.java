package tgw.evolution.mixin;

import com.google.common.base.Suppliers;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.hooks.asm.ModifyStatic;
import tgw.evolution.hooks.asm.RestoreFinal;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.collection.maps.O2OHashMap;
import tgw.evolution.util.collection.maps.O2OMap;
import tgw.evolution.util.collection.sets.OHashSet;
import tgw.evolution.util.collection.sets.OSet;

import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@SuppressWarnings("ClassWithOnlyPrivateConstructors")
@Mixin(PoiType.class)
public abstract class Mixin_CFS_PoiType {

    @Mutable @Shadow @Final @RestoreFinal public static PoiType NITWIT;
    @Mutable @Shadow @Final @RestoreFinal public static PoiType MASON;
    @Mutable @Shadow @Final @RestoreFinal public static PoiType LIBRARIAN;
    @Mutable @Shadow @Final @RestoreFinal public static PoiType LEATHERWORKER;
    @Mutable @Shadow @Final @RestoreFinal public static PoiType FLETCHER;
    @Mutable @Shadow @Final @RestoreFinal public static PoiType SHEPHERD;
    @Mutable @Shadow @Final @RestoreFinal public static PoiType TOOLSMITH;
    @Mutable @Shadow @Final @RestoreFinal public static PoiType WEAPONSMITH;
    @Mutable @Shadow @Final @RestoreFinal public static PoiType HOME;
    @Mutable @Shadow @Final @RestoreFinal public static PoiType MEETING;
    @Mutable @Shadow @Final @RestoreFinal public static PoiType BEEHIVE;
    @Mutable @Shadow @Final @RestoreFinal public static PoiType BEE_NEST;
    @Mutable @Shadow @Final @RestoreFinal public static PoiType NETHER_PORTAL;
    @Mutable @Shadow @Final @RestoreFinal public static PoiType LODESTONE;
    @Mutable @Shadow @Final @RestoreFinal public static PoiType FISHERMAN;
    @Mutable @Shadow @Final @RestoreFinal public static PoiType FARMER;
    @Mutable @Shadow @Final @RestoreFinal public static PoiType CLERIC;
    @Mutable @Shadow @Final @RestoreFinal public static PoiType CARTOGRAPHER;
    @Mutable @Shadow @Final @RestoreFinal public static PoiType BUTCHER;
    @Mutable @Shadow @Final @RestoreFinal public static PoiType ARMORER;
    @Mutable @Shadow @Final @RestoreFinal public static PoiType UNEMPLOYED;
    @Mutable @Shadow @Final @RestoreFinal public static PoiType LIGHTNING_ROD;
    @Mutable @Shadow @Final @RestoreFinal public static Predicate<PoiType> ALL_JOBS;
    @Mutable @Shadow @Final @RestoreFinal public static Predicate<PoiType> ALL;
    @Mutable @Shadow @Final @RestoreFinal public static Map<BlockState, PoiType> TYPE_BY_STATE;
    @Mutable @Shadow @Final @RestoreFinal protected static Set<BlockState> ALL_STATES;
    @Mutable @Shadow @Final @RestoreFinal private static Supplier<Set<PoiType>> ALL_JOB_POI_TYPES;
    @Mutable @Shadow @Final @RestoreFinal private static Set<BlockState> BEDS;
    @Mutable @Shadow @Final @RestoreFinal private static Set<BlockState> CAULDRONS;
    @Mutable @Shadow @Final @RestoreFinal private Set<BlockState> matchingStates;
    @Mutable @Shadow @Final @RestoreFinal private int maxTickets;
    @Mutable @Shadow @Final @RestoreFinal private String name;
    @Mutable @Shadow @Final @RestoreFinal private Predicate<PoiType> predicate;
    @Mutable @Shadow @Final @RestoreFinal private int validRange;

    @ModifyConstructor
    private Mixin_CFS_PoiType(String string, Set<BlockState> set, int maxTickets, Predicate<PoiType> predicate, int validRange) {
        this.name = string;
        this.matchingStates = ((OSet<BlockState>) set).view();
        this.maxTickets = maxTickets;
        this.predicate = predicate;
        this.validRange = validRange;
    }

    @ModifyConstructor
    private Mixin_CFS_PoiType(String string, Set<BlockState> set, int maxTickets, int validRange) {
        this.name = string;
        this.matchingStates = ((OSet<BlockState>) set).view();
        this.maxTickets = maxTickets;
        this.predicate = this::method_19156;
        this.validRange = validRange;
    }

    @Unique
    @ModifyStatic
    private static void clinit() {
        ALL_JOB_POI_TYPES = Suppliers.memoize(() -> Registry.VILLAGER_PROFESSION.stream().map(VillagerProfession::getJobPoiType).collect(Collectors.toSet()));
        ALL_JOBS = poiType -> ALL_JOB_POI_TYPES.get().contains(poiType);
        ALL = poiType -> true;
        OSet<BlockState> bedSet = new OHashSet<>();
        Block[] beds = {Blocks.RED_BED, Blocks.BLACK_BED, Blocks.BLUE_BED, Blocks.BROWN_BED, Blocks.CYAN_BED, Blocks.GRAY_BED, Blocks.GREEN_BED, Blocks.LIGHT_BLUE_BED, Blocks.LIGHT_GRAY_BED, Blocks.LIME_BED, Blocks.MAGENTA_BED, Blocks.ORANGE_BED, Blocks.PINK_BED, Blocks.PURPLE_BED, Blocks.WHITE_BED, Blocks.YELLOW_BED};
        for (Block bed : beds) {
            OList<BlockState> possibleStates = bed.getStateDefinition().getPossibleStates_();
            for (int i = 0, len = possibleStates.size(); i < len; ++i) {
                BlockState state = possibleStates.get(i);
                if (state.getValue(BedBlock.PART) == BedPart.HEAD) {
                    bedSet.add(state);
                }
            }
        }
        bedSet.trim();
        BEDS = bedSet.view();
        OSet<BlockState> cauldronSet = new OHashSet<>();
        Block[] cauldrons = {Blocks.CAULDRON, Blocks.LAVA_CAULDRON, Blocks.WATER_CAULDRON, Blocks.POWDER_SNOW_CAULDRON};
        for (Block cauldron : cauldrons) {
            OList<BlockState> possibleStates = cauldron.getStateDefinition().getPossibleStates_();
            for (int i = 0, len = possibleStates.size(); i < len; ++i) {
                cauldronSet.add(possibleStates.get(i));
            }
        }
        cauldronSet.trim();
        CAULDRONS = cauldronSet.view();
        O2OMap<BlockState, PoiType> map = new O2OHashMap<>();
        TYPE_BY_STATE = map;
        UNEMPLOYED = register("unemployed", OSet.emptySet(), 1, ALL_JOBS, 1);
        ARMORER = register("armorer", getBlockStates(Blocks.BLAST_FURNACE), 1, 1);
        BUTCHER = register("butcher", getBlockStates(Blocks.SMOKER), 1, 1);
        CARTOGRAPHER = register("cartographer", getBlockStates(Blocks.CARTOGRAPHY_TABLE), 1, 1);
        CLERIC = register("cleric", getBlockStates(Blocks.BREWING_STAND), 1, 1);
        FARMER = register("farmer", getBlockStates(Blocks.COMPOSTER), 1, 1);
        FISHERMAN = register("fisherman", getBlockStates(Blocks.BARREL), 1, 1);
        FLETCHER = register("fletcher", getBlockStates(Blocks.FLETCHING_TABLE), 1, 1);
        LEATHERWORKER = register("leatherworker", CAULDRONS, 1, 1);
        LIBRARIAN = register("librarian", getBlockStates(Blocks.LECTERN), 1, 1);
        MASON = register("mason", getBlockStates(Blocks.STONECUTTER), 1, 1);
        NITWIT = register("nitwit", OSet.emptySet(), 1, 1);
        SHEPHERD = register("shepherd", getBlockStates(Blocks.LOOM), 1, 1);
        TOOLSMITH = register("toolsmith", getBlockStates(Blocks.SMITHING_TABLE), 1, 1);
        WEAPONSMITH = register("weaponsmith", getBlockStates(Blocks.GRINDSTONE), 1, 1);
        HOME = register("home", BEDS, 1, 1);
        MEETING = register("meeting", getBlockStates(Blocks.BELL), 32, 6);
        BEEHIVE = register("beehive", getBlockStates(Blocks.BEEHIVE), 0, 1);
        BEE_NEST = register("bee_nest", getBlockStates(Blocks.BEE_NEST), 0, 1);
        NETHER_PORTAL = register("nether_portal", getBlockStates(Blocks.NETHER_PORTAL), 0, 1);
        LODESTONE = register("lodestone", getBlockStates(Blocks.LODESTONE), 0, 1);
        LIGHTNING_ROD = register("lightning_rod", getBlockStates(Blocks.LIGHTNING_ROD), 0, 1);
        OSet<BlockState> set = new OHashSet<>();
        for (long it = map.beginIteration(); map.hasNextIteration(it); it = map.nextEntry(it)) {
            set.add(map.getIterationKey(it));
        }
        ALL_STATES = set;
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    private static Set<BlockState> getBlockStates(Block block) {
        return new OHashSet<>(block.getStateDefinition().getPossibleStates_()).view();
    }

    @Shadow
    private static PoiType register(String string, Set<BlockState> set, int i, Predicate<PoiType> predicate, int j) {
        throw new AbstractMethodError();
    }

    @Shadow
    private static PoiType register(String string, Set<BlockState> set, int i, int j) {
        throw new AbstractMethodError();
    }

    @Shadow
    protected abstract boolean method_19156(PoiType par1);
}
