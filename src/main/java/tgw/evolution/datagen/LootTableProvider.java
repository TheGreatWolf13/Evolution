//package tgw.evolution.datagen;
//
//import com.google.common.collect.ImmutableList;
//import com.google.common.collect.Multimap;
//import com.mojang.datafixers.util.Pair;
//import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
//import net.minecraft.data.DataGenerator;
//import net.minecraft.data.HashCache;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.world.level.storage.loot.LootTable;
//import net.minecraft.world.level.storage.loot.LootTables;
//import net.minecraft.world.level.storage.loot.ValidationContext;
//import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
//import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
//import tgw.evolution.Evolution;
//import tgw.evolution.datagen.loot.BlockLootTables;
//import tgw.evolution.util.collection.maps.O2OHashMap;
//import tgw.evolution.util.collection.maps.O2OMap;
//
//import java.nio.file.Path;
//import java.util.Collection;
//import java.util.List;
//import java.util.Map;
//import java.util.function.BiConsumer;
//import java.util.function.Consumer;
//import java.util.function.Supplier;
//
//public class LootTableProvider implements EvolutionDataProvider<ResourceLocation> {
//
//    private static final List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>>
//            SUBPROVIDERS = ImmutableList.of(Pair.of(BlockLootTables::new, LootContextParamSets.BLOCK));
//
//    protected final DataGenerator generator;
//    private final Collection<Path> existingPaths;
//
//    public LootTableProvider(GenBundle bundle) {
//        this.generator = bundle.generator();
//        this.existingPaths = bundle.existingPaths();
//    }
//
//    private Path createPath(ResourceLocation id) {
//        return this.generator.getOutputFolder().resolve(this.makePath(id));
//    }
//
//    @Override
//    public Collection<Path> existingPaths() {
//        return this.existingPaths;
//    }
//
//    @Override
//    public String getName() {
//        return "Evolution LootTables";
//    }
//
//    protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> getTables() {
//        return SUBPROVIDERS;
//    }
//
//    @Override
//    public String makePath(ResourceLocation id) {
//        return "data/" + id.getNamespace() + "/loot_tables/" + id.getPath() + ".json";
//    }
//
//    @Override
//    public void run(HashCache cache) {
//        O2OMap<ResourceLocation, LootTable> map = new O2OHashMap<>();
//        List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> tables = this.getTables();
//        for (int i = 0, len = tables.size(); i < len; i++) {
//            Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet> pair = tables.get(i);
//            //noinspection ObjectAllocationInLoop
//            pair.getFirst().get().accept((r, b) -> {
//                if (map.put(r, b.setParamSet(pair.getSecond()).build()) != null) {
//                    throw new IllegalStateException("Duplicate loot table " + r);
//                }
//            });
//        }
//        //noinspection ReturnOfNull
//        ValidationContext validation = new ValidationContext(LootContextParamSets.ALL_PARAMS, r -> null, map::get);
//        this.validate(map, validation);
//        Multimap<String, String> problems = validation.getProblems();
//        if (!problems.isEmpty()) {
//            for (Map.Entry<String, String> entry : problems.entries()) {
//                Evolution.warn("Found validation problem in {}: {}", entry.getKey(), entry.getValue());
//            }
//            throw new IllegalStateException("Failed to validate loot tables, see logs");
//        }
//        for (Object2ObjectMap.Entry<ResourceLocation, LootTable> entry : map.object2ObjectEntrySet()) {
//            Path path = this.createPath(entry.getKey());
//            this.save(cache, LootTables.serialize(entry.getValue()), path, entry.getKey());
//        }
//    }
//
//    @Override
//    public String type() {
//        return "LootTable";
//    }
//
//    protected void validate(Map<ResourceLocation, LootTable> map, ValidationContext validationTracker) {
//        for (Map.Entry<ResourceLocation, LootTable> entry : map.entrySet()) {
//            LootTables.validate(validationTracker, entry.getKey(), entry.getValue());
//        }
//    }
//}
