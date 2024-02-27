package tgw.evolution.world.lighting;

import net.minecraft.world.level.LevelHeightAccessor;

public final class WorldUtil {

    private WorldUtil() {
        throw new RuntimeException();
    }

    public static int getMaxBlockY(LevelHeightAccessor world) {
        return getMaxSection(world) << 4 | 15;
    }

    public static int getMaxLightSection(LevelHeightAccessor world) {
        return getMaxSection(world) + 1;
    }

    public static int getMaxSection(LevelHeightAccessor world) {
        return world.getMaxSection() - 1; // getMaxSection() is exclusive
    }

    public static int getMinBlockY(LevelHeightAccessor world) {
        return getMinSection(world) << 4;
    }

    public static int getMinLightSection(LevelHeightAccessor world) {
        return getMinSection(world) - 1;
    }

    public static int getMinSection(LevelHeightAccessor world) {
        return world.getMinSection();
    }

    public static int getTotalLightSections(LevelHeightAccessor world) {
        return getMaxLightSection(world) - getMinLightSection(world) + 1;
    }

    public static int getTotalSections(LevelHeightAccessor world) {
        return getMaxSection(world) - getMinSection(world) + 1;
    }

}