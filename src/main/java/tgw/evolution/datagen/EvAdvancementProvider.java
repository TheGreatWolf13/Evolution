package tgw.evolution.datagen;

import com.google.common.collect.ImmutableList;
import net.minecraft.advancements.Advancement;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import tgw.evolution.datagen.advancements.StoneAge;

import java.util.List;
import java.util.function.Consumer;

public class EvAdvancementProvider extends AdvancementProvider {

    private final List<Consumer<Consumer<Advancement>>> tabs = ImmutableList.of(new StoneAge());

    public EvAdvancementProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, existingFileHelper);
    }

    @Override
    protected void registerAdvancements(Consumer<Advancement> consumer, ExistingFileHelper fileHelper) {
        for (int i = 0, len = this.tabs.size(); i < len; i++) {
            this.tabs.get(i).accept(consumer);
        }
    }
}
