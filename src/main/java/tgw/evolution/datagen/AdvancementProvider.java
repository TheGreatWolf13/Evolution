package tgw.evolution.datagen;

import com.google.common.collect.ImmutableList;
import net.minecraft.advancements.Advancement;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import tgw.evolution.datagen.advancements.StoneAge;
import tgw.evolution.util.collection.sets.OHashSet;
import tgw.evolution.util.collection.sets.OSet;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class AdvancementProvider implements EvolutionDataProvider<Advancement> {

    protected final DataGenerator generator;
    private final Collection<Path> existingPaths;
    private final List<Consumer<Consumer<Advancement>>> tabs = ImmutableList.of(new StoneAge());

    public AdvancementProvider(DataGenerator generator, Collection<Path> existingPaths) {
        this.generator = generator;
        this.existingPaths = existingPaths;
    }

    private Path createPath(Advancement advancement) {
        return this.generator.getOutputFolder().resolve(this.makePath(advancement));
    }

    @Override
    public Collection<Path> existingPaths() {
        return this.existingPaths;
    }

    @Override
    public String getName() {
        return "Evolution Advancements";
    }

    @Override
    public String makePath(Advancement id) {
        return "data/" + id.getId().getNamespace() + "/advancements/" + id.getId().getPath() + ".json";
    }

    protected void registerAdvancements(Consumer<Advancement> consumer) {
        for (int i = 0, len = this.tabs.size(); i < len; i++) {
            this.tabs.get(i).accept(consumer);
        }
    }

    @Override
    public void run(HashCache cache) {
        OSet<ResourceLocation> set = new OHashSet<>();
        Consumer<Advancement> consumer = advancement -> {
            if (!set.add(advancement.getId())) {
                throw new IllegalStateException("Duplicate advancement " + advancement.getId());
            }
            Path path = this.createPath(advancement);
            this.save(cache, advancement.deconstruct().serializeToJson(), path, advancement);
        };
        this.registerAdvancements(consumer);
    }

    @Override
    public String type() {
        return "Advancement";
    }
}
