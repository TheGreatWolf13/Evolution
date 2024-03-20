package tgw.evolution.util.constants;

import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.UnmodifiableView;
import tgw.evolution.init.IVariant;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;

import java.util.Map;

public enum NutrientVariant implements IVariant<NutrientVariant> {
    POOR("poor"),
    NORMAL("normal"),
    RICH("rich");

    public static final NutrientVariant[] VALUES = values();
    private static final OList<Map<NutrientVariant, ? extends Block>> BLOCKS = new OArrayList<>();
    private final String name;

    NutrientVariant(String name) {
        this.name = name;
    }

    @Override
    public @UnmodifiableView OList<Map<NutrientVariant, ? extends Block>> getBlocks() {
        return BLOCKS.view();
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void registerBlocks(Map<NutrientVariant, ? extends Block> blocks) {
        BLOCKS.add(blocks);
    }
}
