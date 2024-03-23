package tgw.evolution.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import tgw.evolution.init.EvolutionFormatter;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.collection.sets.RHashSet;
import tgw.evolution.util.collection.sets.RSet;

public final class EvolutionConfig {

    //Root
    public static final ConfigFolder ROOT = new ConfigFolder(null, "root");
    public static final ConfigBoolean FOLLOW_UPS = new ConfigBoolean(ROOT, "followUps", true);
    public static final ConfigBoolean HITMARKERS = new ConfigBoolean(ROOT, "hitmarkers", true);
    //Dynamic Lighting
    public static final ConfigFolder DYNAMIC_LIGHTING = new ConfigFolder(ROOT, "dynamicLighting");
    public static final ConfigBoolean DL_ENABLED = new ConfigBoolean(DYNAMIC_LIGHTING, "enabled", true);
    public static final ConfigInteger DL_TICKRATE = new ConfigInteger(DYNAMIC_LIGHTING, "tickrate", 1, 20, 4);
    public static final ConfigBoolean DL_ITEMS = new ConfigBoolean(DYNAMIC_LIGHTING, "items", true);
    public static final ConfigBoolean DL_ENTITIES = new ConfigBoolean(DYNAMIC_LIGHTING, "entities", true);
    //Performance
    public static final ConfigFolder PERFORMANCE = new ConfigFolder(ROOT, "performance");
    public static final ConfigBoolean ANIMATED_TEXTURES = new ConfigBoolean(PERFORMANCE, "animatedTextures", true);
    public static final ConfigInteger LEAVES_CULLING = new ConfigInteger(PERFORMANCE, "leavesCulling", 0, 8, 3);
    public static final ConfigBoolean SHOW_PLANETS = new ConfigBoolean(PERFORMANCE, "showPlanets", true);
    public static final ConfigBoolean SYNC_RENDERING = new ConfigBoolean(PERFORMANCE, "syncRendering", false);
    //Units
    public static final ConfigFolder UNITS = new ConfigFolder(ROOT, "units");
    public static final ConfigEnum<EvolutionFormatter.Temperature> BODY_TEMPERATURE = new ConfigEnum<>(UNITS, "bodyTemperature", EvolutionFormatter.Temperature.VALUES, EvolutionFormatter.Temperature.CELSIUS);
    public static final ConfigEnum<EvolutionFormatter.Distance> DISTANCE = new ConfigEnum<>(UNITS, "distance", EvolutionFormatter.Distance.VALUES, EvolutionFormatter.Distance.METRIC);
    public static final ConfigEnum<EvolutionFormatter.Drink> DRINK = new ConfigEnum<>(UNITS, "drink", EvolutionFormatter.Drink.VALUES, EvolutionFormatter.Drink.MILLILITER);
    public static final ConfigEnum<EvolutionFormatter.Food> FOOD = new ConfigEnum<>(UNITS, "food", EvolutionFormatter.Food.VALUES, EvolutionFormatter.Food.KILOCALORIE);
    public static final ConfigBoolean LIMIT_TIME_UNITS_TO_HOUR = new ConfigBoolean(UNITS, "limitTimeUnitsToHour", false);
    public static final ConfigEnum<EvolutionFormatter.Mass> MASS = new ConfigEnum<>(UNITS, "mass", EvolutionFormatter.Mass.VALUES, EvolutionFormatter.Mass.KILOGRAM);
    public static final ConfigEnum<EvolutionFormatter.Speed> SPEED = new ConfigEnum<>(UNITS, "speed", EvolutionFormatter.Speed.VALUES, EvolutionFormatter.Speed.METERS_PER_SECOND);
    public static final ConfigEnum<EvolutionFormatter.Volume> VOLUME = new ConfigEnum<>(UNITS, "volume", EvolutionFormatter.Volume.VALUES, EvolutionFormatter.Volume.LITER);
    //Debug
    public static final ConfigFolder DEBUG = new ConfigFolder(ROOT, "debug");
    public static final ConfigBoolean RENDER_HEIGHTMAP = new ConfigBoolean(DEBUG, "renderHeightmap", false);
    //      Sky
    public static final ConfigFolder SKY = new ConfigFolder(DEBUG, "sky");
    public static final ConfigBoolean CELESTIAL_FORCE_ALL = new ConfigBoolean(SKY, "celestialForceAll", false);
    public static final ConfigBoolean CELESTIAL_EQUATOR = new ConfigBoolean(SKY, "celestialEquator", false);
    public static final ConfigBoolean CELESTIAL_POLES = new ConfigBoolean(SKY, "celestialPoles", false);
    public static final ConfigBoolean ECLIPTIC = new ConfigBoolean(SKY, "ecliptic", false);
    public static final ConfigBoolean SUN_PATH = new ConfigBoolean(SKY, "sunPath", false);
    public static final ConfigBoolean PLANETS = new ConfigBoolean(SKY, "planets", false);
    private static final RSet<IConfigItem> DIRTY = new RHashSet<>();
    private static final RSet<IConfigItem> NEEDS_RESTORATION = new RHashSet<>();
    private static final int VERSION = 1;
    //
    public static boolean toggleCrawl = true;
    public static int version;
    private static boolean isDirty;
    private static byte needsRestoration = -1;

    static {
        CELESTIAL_FORCE_ALL.setPriority(IConfigItem.Priority.HIGH);
        LIMIT_TIME_UNITS_TO_HOUR.setPriority(IConfigItem.Priority.HIGH);
        DL_ENABLED.setPriority(IConfigItem.Priority.HIGH);
    }

    private EvolutionConfig() {
    }

    private static void analyseFolder(ConfigFolder folder) {
        OList<IConfigItem> items = folder.items();
        for (int i = 0, len = items.size(); i < len; ++i) {
            IConfigItem item = items.get(i);
            if (item.type() == IConfigItem.Type.FOLDER) {
                analyseFolder((ConfigFolder) item);
            }
            else {
                if (!item.isDefault()) {
                    NEEDS_RESTORATION.add(item);
                }
            }
        }
    }

    public static void discardDirty() {
        for (long it = DIRTY.beginIteration(); (it & 0xFFFF_FFFFL) != 0; it = DIRTY.nextEntry(it)) {
            IConfigItem item = DIRTY.getIteration(it);
            item.discardDirty();
            if (item.isDefault()) {
                NEEDS_RESTORATION.remove(item);
            }
            else {
                NEEDS_RESTORATION.add(item);
            }
        }
        DIRTY.clear();
        isDirty = false;
        needsRestoration = (byte) (NEEDS_RESTORATION.isEmpty() ? 0 : 1);
    }

    public static void handle(IConfigItem config) {
        if (config.isDirty()) {
            DIRTY.add(config);
        }
        else {
            DIRTY.remove(config);
        }
        isDirty = !DIRTY.isEmpty();
        if (config.isDefault()) {
            NEEDS_RESTORATION.remove(config);
        }
        else {
            NEEDS_RESTORATION.add(config);
        }
        needsRestoration = (byte) (NEEDS_RESTORATION.isEmpty() ? 0 : 1);
    }

    public static boolean isDirty() {
        return isDirty;
    }

    public static void load() {
        needsRestoration = -1;
    }

    public static boolean needsRestoration() {
        if (needsRestoration == -1) {
            NEEDS_RESTORATION.clear();
            analyseFolder(ROOT);
            needsRestoration = (byte) (NEEDS_RESTORATION.isEmpty() ? 0 : 1);
        }
        return needsRestoration != 0;
    }

    public static boolean needsWelcome() {
        return version != VERSION;
    }

    public static void processOptions(Options.FieldAccess acc) {
        toggleCrawl = acc.process("toggleCrawl", toggleCrawl);
        version = acc.process("evolutionVersion", version);
        //Root
        FOLLOW_UPS.setAndSave(acc.process("followUps", FOLLOW_UPS.get()));
        HITMARKERS.setAndSave(acc.process("hitmarkers", HITMARKERS.get()));
        //Performance
        ANIMATED_TEXTURES.setAndSave(acc.process("animatedTextures", ANIMATED_TEXTURES.get()));
        LEAVES_CULLING.setAndSave(acc.process("leavesCulling", LEAVES_CULLING.get()));
        SHOW_PLANETS.setAndSave(acc.process("showPlanets", SHOW_PLANETS.get()));
        SYNC_RENDERING.setAndSave(acc.process("syncRendering", SYNC_RENDERING.get()));
        //Units
        BODY_TEMPERATURE.setAndSave(acc.process("bodyTemperature", BODY_TEMPERATURE.get(), BODY_TEMPERATURE::byId, Enum::ordinal));
        DISTANCE.setAndSave(acc.process("distance", DISTANCE.get(), DISTANCE::byId, Enum::ordinal));
        DRINK.setAndSave(acc.process("drink", DRINK.get(), DRINK::byId, Enum::ordinal));
        FOOD.setAndSave(acc.process("food", FOOD.get(), FOOD::byId, Enum::ordinal));
        LIMIT_TIME_UNITS_TO_HOUR.setAndSave(acc.process("limitTimeUnitsToHour", LIMIT_TIME_UNITS_TO_HOUR.get()));
        MASS.setAndSave(acc.process("mass", MASS.get(), MASS::byId, Enum::ordinal));
        SPEED.setAndSave(acc.process("speed", SPEED.get(), SPEED::byId, Enum::ordinal));
        VOLUME.setAndSave(acc.process("volume", VOLUME.get(), VOLUME::byId, Enum::ordinal));
        //Debug
        RENDER_HEIGHTMAP.setAndSave(acc.process("renderHeightmap", RENDER_HEIGHTMAP.get()));
        //      Sky
        CELESTIAL_FORCE_ALL.setAndSave(acc.process("celestialForceAll", CELESTIAL_FORCE_ALL.get()));
        CELESTIAL_EQUATOR.setAndSave(acc.process("celestialEquator", CELESTIAL_EQUATOR.get()));
        CELESTIAL_POLES.setAndSave(acc.process("celestialPoles", CELESTIAL_POLES.get()));
        ECLIPTIC.setAndSave(acc.process("ecliptic", ECLIPTIC.get()));
        SUN_PATH.setAndSave(acc.process("sunPath", SUN_PATH.get()));
        PLANETS.setAndSave(acc.process("planets", PLANETS.get()));
    }

    public static void restore() {
        ROOT.restore();
        needsRestoration = 0;
        NEEDS_RESTORATION.clear();
    }

    public static void save() {
        if (isDirty) {
            for (long it = DIRTY.beginIteration(); (it & 0xFFFF_FFFFL) != 0; it = DIRTY.nextEntry(it)) {
                DIRTY.getIteration(it).save();
            }
            DIRTY.clear();
            isDirty = false;
            Minecraft mc = Minecraft.getInstance();
            mc.options.save();
            mc.lvlRenderer().allChanged();
        }
    }

    public static void updateWelcome() {
        version = VERSION;
        Minecraft.getInstance().options.save();
    }
}
