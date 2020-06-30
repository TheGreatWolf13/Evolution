package tgw.evolution.world.puzzle;

import com.google.common.collect.Maps;
import net.minecraft.util.ResourceLocation;

import java.util.Map;

public class PuzzlePatternRegistry {

    private final Map<ResourceLocation, PuzzlePattern> registry = Maps.newHashMap();

    public PuzzlePatternRegistry() {
        this.register(PuzzlePattern.EMPTY);
    }

    public void register(PuzzlePattern pattern) {
        this.registry.put(pattern.getPool(), pattern);
    }

    public PuzzlePattern get(ResourceLocation name) {
        PuzzlePattern pattern = this.registry.get(name);
        return pattern != null ? pattern : PuzzlePattern.INVALID;
    }
}
